import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8081/api/v1';
const pageNo = __ENV.PAGE_NO || '1';
const pageSize = __ENV.PAGE_SIZE || '10';
const readerUserId = __ENV.READER_USER_ID || '101';
const readerNickname = __ENV.READER_NICKNAME || 'Noteit Reader';
const authorUserId = __ENV.AUTHOR_USER_ID || '102';
const authorNickname = __ENV.AUTHOR_NICKNAME || 'Noteit Author';
const setupArticles = Number.parseInt(__ENV.SETUP_ARTICLES || '0', 10);
const sleepSeconds = Number.parseFloat(__ENV.SLEEP_SECONDS || '1');

export const options = {
  vus: Number.parseInt(__ENV.VUS || '10', 10),
  duration: __ENV.DURATION || '30s',
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
  thresholds: {
    http_req_failed: ['rate<0.01'],
  },
};

export function setup() {
  ensureFollow();
  for (let i = 0; i < setupArticles; i += 1) {
    createArticle(i);
  }
}

export default function () {
  const response = http.get(`${baseUrl}/users/me/feed?pageNo=${pageNo}&pageSize=${pageSize}`, {
    headers: {
      'X-User-Id': readerUserId,
      'X-User-Nickname': readerNickname,
    },
  });
  const body = parseJson(response);

  check(response, {
    'status is 200': (res) => res.status === 200,
    'business code is success': () => body !== null && body.code === '0',
    'records is array': () => body !== null && body.data !== undefined && Array.isArray(body.data.records),
  });

  if (sleepSeconds > 0) {
    sleep(sleepSeconds);
  }
}

function ensureFollow() {
  const response = http.put(`${baseUrl}/users/${authorUserId}/follow`, null, {
    headers: {
      'X-User-Id': readerUserId,
      'X-User-Nickname': readerNickname,
    },
  });

  check(response, {
    'setup follow succeeds': (res) => {
      const body = parseJson(res);
      return res.status === 200 && body !== null && body.code === '0';
    },
  });
}

function createArticle(index) {
  const token = `${Date.now()}-${index}`;
  const payload = JSON.stringify({
    title: `k6 following feed article ${token}`,
    content: `k6 following feed baseline content ${token}`,
    contentFormat: 'MARKDOWN',
    contentPreview: `k6 following feed preview ${token}`,
    coverObjectKey: `article/image/load-test/${token}.jpg`,
    coverUrl: `https://cdn.noteit.test/article/image/load-test/${token}.jpg`,
  });

  const response = http.post(`${baseUrl}/articles`, payload, {
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': authorUserId,
      'X-User-Nickname': authorNickname,
    },
  });

  check(response, {
    'setup article create succeeds': (res) => {
      const body = parseJson(res);
      return res.status === 200 && body !== null && body.code === '0';
    },
  });
}

function parseJson(response) {
  if (!response || !response.body) {
    return null;
  }
  try {
    return response.json();
  } catch (_) {
    return null;
  }
}
