import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080/api/v1';
const articleId = __ENV.ARTICLE_ID || '1006';
const userId = __ENV.USER_ID || '101';
const userNickname = __ENV.USER_NICKNAME || 'Noteit_load_test';
const thinkTimeSeconds = Number(__ENV.SLEEP_SECONDS || '1');

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
  const response = http.get(
    `${baseUrl}/articles/${articleId}`,
    {
      headers: {
        'X-User-Id': userId,
        'X-User-Nickname': userNickname,
      },
      tags: {
        endpoint: 'GET /articles/{articleId}',
        scenario: 'article-detail-read',
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
        return body && body.data && body.data.id === articleId;
      } catch (error) {
        return false;
      }
    },
  });

  if (thinkTimeSeconds > 0) {
    sleep(thinkTimeSeconds);
  }
}
