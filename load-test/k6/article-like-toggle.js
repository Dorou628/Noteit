import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080/api/v1';
const thinkTimeSeconds = Number(__ENV.SLEEP_SECONDS || '1');

// Existing active like relations in the local MVP database.
// We intentionally reuse existing rows because the current LocalIdGenerator
// may collide on new inserts during load, which would distort the write baseline.
const likeCombos = [
  { articleId: '1001', userId: '103' },
  { articleId: '1002', userId: '101' },
  { articleId: '1003', userId: '101' },
  { articleId: '1003', userId: '102' },
  { articleId: '1003', userId: '103' },
  { articleId: '1004', userId: '101' },
  { articleId: '1004', userId: '102' },
  { articleId: '1005', userId: '101' },
  { articleId: '1005', userId: '103' },
  { articleId: '1006', userId: '101' },
  { articleId: '10005', userId: '101' },
  { articleId: '10006', userId: '102' },
  { articleId: '10007', userId: '101' },
];

export const options = {
  vus: Number(__ENV.VUS || '10'),
  duration: __ENV.DURATION || '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<300', 'p(99)<800'],
    checks: ['rate>0.99'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

export default function () {
  const combo = likeCombos[(exec.vu.idInTest - 1) % likeCombos.length];
  const shouldLike = exec.vu.iterationInScenario % 2 === 1;
  const method = shouldLike ? 'PUT' : 'DELETE';
  const response = http.request(
    method,
    `${baseUrl}/articles/${combo.articleId}/like`,
    null,
    {
      headers: {
        'X-User-Id': combo.userId,
        'X-User-Nickname': `LoadUser_${combo.userId}`,
      },
      tags: {
        endpoint: shouldLike ? 'PUT /articles/{articleId}/like' : 'DELETE /articles/{articleId}/like',
        scenario: 'article-like-toggle',
        article_id: combo.articleId,
        user_id: combo.userId,
      },
    }
  );

  check(response, {
    'status is 200': (res) => res.status === 200,
    'response code is success': (res) => {
      try {
        const body = res.json();
        return body && body.code === '0';
      } catch (error) {
        return false;
      }
    },
    'article id matches': (res) => {
      try {
        const body = res.json();
        return body && body.data && body.data.articleId === combo.articleId;
      } catch (error) {
        return false;
      }
    },
  });

  if (thinkTimeSeconds > 0) {
    sleep(thinkTimeSeconds);
  }
}
