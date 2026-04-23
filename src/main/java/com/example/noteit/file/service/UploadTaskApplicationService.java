package com.example.noteit.file.service;

import com.example.noteit.common.constant.ErrorCode;
import com.example.noteit.common.exception.BusinessException;
import com.example.noteit.common.id.IdGenerator;
import com.example.noteit.common.util.TimeProvider;
import com.example.noteit.file.domain.OssPresignGateway;
import com.example.noteit.file.domain.OssPresignRequest;
import com.example.noteit.file.domain.OssPresignResult;
import com.example.noteit.file.model.CompleteUploadTaskRequest;
import com.example.noteit.file.model.CreateUploadTaskRequest;
import com.example.noteit.file.model.UploadTaskDO;
import com.example.noteit.file.model.UploadTaskResponse;
import com.example.noteit.file.model.UploadTaskStatus;
import com.example.noteit.file.repository.UploadTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class UploadTaskApplicationService {

    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;
    private final UploadTaskRepository uploadTaskRepository;
    private final OssPresignGateway ossPresignGateway;

    public UploadTaskApplicationService(
            IdGenerator idGenerator,
            TimeProvider timeProvider,
            UploadTaskRepository uploadTaskRepository,
            OssPresignGateway ossPresignGateway
    ) {
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
        this.uploadTaskRepository = uploadTaskRepository;
        this.ossPresignGateway = ossPresignGateway;
    }

    @Transactional
    public UploadTaskResponse createUploadTask(Long userId, CreateUploadTaskRequest request) {
        // 这里只负责“生成上传任务 + 持久化元数据”，不直接处理二进制文件。
        validateCreateRequest(request);

        long uploadTaskId = idGenerator.nextId();
        OffsetDateTime expiredAt = timeProvider.now().plusMinutes(15);
        String objectKey = buildObjectKey(uploadTaskId, request);
        OssPresignResult presignResult = ossPresignGateway.presignUpload(new OssPresignRequest(
                objectKey,
                request.contentType(),
                request.contentLength(),
                expiredAt
        ));

        uploadTaskRepository.insert(new UploadTaskDO(
                uploadTaskId,
                userId,
                request.bizType().name(),
                request.fileName(),
                request.contentType(),
                request.contentLength(),
                objectKey,
                presignResult.publicUrl(),
                UploadTaskStatus.CREATED.code(),
                null,
                expiredAt.toLocalDateTime(),
                null,
                null
        ));

        return new UploadTaskResponse(
                String.valueOf(uploadTaskId),
                objectKey,
                presignResult.uploadMethod(),
                presignResult.uploadUrl(),
                presignResult.headers(),
                presignResult.formFields(),
                presignResult.expiredAt(),
                presignResult.publicUrl(),
                UploadTaskStatus.CREATED.name()
        );
    }

    @Transactional
    public UploadTaskResponse completeUploadTask(Long userId, String uploadTaskId, CompleteUploadTaskRequest request) {
        // 当前 MVP 的“完成上传”只做任务状态确认，不校验 OSS 中对象是否真实存在。
        long uploadTaskIdValue = parseId(uploadTaskId);
        UploadTaskDO existing = uploadTaskRepository.findById(uploadTaskIdValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.UPLOAD_TASK_NOT_FOUND));
        if (existing.userId() != userId) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        uploadTaskRepository.markConfirmed(uploadTaskIdValue, userId, request.etag());

        UploadTaskDO confirmed = uploadTaskRepository.findById(uploadTaskIdValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.UPLOAD_TASK_NOT_FOUND));
        return toResponse(confirmed);
    }

    private void validateCreateRequest(CreateUploadTaskRequest request) {
        // 先做最小校验，避免无意义任务写库。
        if (!StringUtils.hasText(request.fileName()) || request.contentLength() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
    }

    private String buildObjectKey(long uploadTaskId, CreateUploadTaskRequest request) {
        // objectKey 统一由后端生成，前端不直接决定 OSS 路径，避免目录规则散落到客户端。
        String datePath = timeProvider.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String normalizedFileName = request.fileName().replace(' ', '-');
        String fileSegment = uploadTaskId + "-" + normalizedFileName;
        return switch (request.bizType()) {
            // TODO: Keep ARTICLE_CONTENT as a reserved OSS path until body storage switches back from DB to OSS.
            case ARTICLE_CONTENT -> "article/content/" + datePath + "/" + fileSegment;
            case ARTICLE_IMAGE -> "article/image/" + datePath + "/" + fileSegment;
            case AVATAR -> "avatar/" + datePath + "/" + fileSegment;
        };
    }

    private UploadTaskResponse toResponse(UploadTaskDO uploadTask) {
        // 完成上传后不再返回新的签名，只返回任务结果和对外访问地址。
        return new UploadTaskResponse(
                String.valueOf(uploadTask.id()),
                uploadTask.objectKey(),
                null,
                null,
                Map.of(),
                Map.of(),
                toOffsetDateTime(uploadTask.expiredAt()),
                uploadTask.publicUrl(),
                UploadTaskStatus.fromCode(uploadTask.status()).name()
        );
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime value) {
        return value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private long parseId(String rawId) {
        // 外部 ID 统一走字符串，进入应用层后再转 long。
        try {
            return Long.parseLong(rawId);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.UPLOAD_TASK_NOT_FOUND);
        }
    }
}
