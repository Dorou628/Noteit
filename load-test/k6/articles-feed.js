import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8081/api/v1';
const pageNo = __ENV.PAGE_NO || '1';
const pageSize = __ENV.PAGE_SIZE || '10';
const userId = __ENV.USER_ID || '101';
const userNickname = __ENV.USER_NICKNAME || 'Noteit_load_test';
const sleepSeconds = Number.parseFloat(__ENV.SLEEP_SECONDS || '1');

export const options = {
  vus: Number.parseInt(__ENV.VUS || '10', 10),
  duration: __ENV.DURATION || '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<300', 'p(99)<800'],
    checks: ['rate>0.99'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

export default function () {
  const response = http.get(`${baseUrl}/articles?pageNo=${pageNo}&pageSize=${pageSize}`, {
    headers: {
      'X-User-Id': userId,
      'X-User-Nickname': userNickname,
    },
    tags: {
      endpoint: 'GET /articles',
      scenario: 'feed-read',
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
