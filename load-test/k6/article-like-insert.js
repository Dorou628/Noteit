import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080/api/v1';
const articleId = __ENV.ARTICLE_ID || '1004';
const userIdBase = Number(__ENV.USER_ID_BASE || '900000000');
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

function buildUniqueUserId() {
  return String(userIdBase + exec.vu.idInTest * 1_000_000 + exec.vu.iterationInScenario);
}

export default function () {
  const userId = buildUniqueUserId();
  const response = http.put(
    `${baseUrl}/articles/${articleId}/like`,
    null,
    {
      headers: {
        'X-User-Id': userId,
        'X-User-Nickname': `LoadInsert_${userId}`,
      },
      tags: {
        endpoint: 'PUT /articles/{articleId}/like',
        scenario: 'article-like-insert',
        article_id: articleId,
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
        return body && body.data && body.data.articleId === articleId;
      } catch (error) {
        return false;
      }
    },
    'liked is true': (res) => {
      try {
        const body = res.json();
        return body && body.data && body.data.liked === true;
      } catch (error) {
        return false;
      }
    },
  });

  if (thinkTimeSeconds > 0) {
    sleep(thinkTimeSeconds);
  }
}
