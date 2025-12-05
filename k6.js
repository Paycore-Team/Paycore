import http from 'k6/http';
import { check } from 'k6';

// 테스트 런 식별자 (원하면 동적으로 -e TEST_RUN_ID 로 받아도 됨)
const TEST_RUN_ID = __ENV.TEST_RUN_ID || 'LOCAL_RUN';

export const options = {
  scenarios: {
    orders_rps: {
      executor: 'constant-arrival-rate',
      rate: 3000,
      timeUnit: '1s',
      duration: '10s',
      preAllocatedVUs: 50,
      maxVUs: 100,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],   // 실패율 < 1%
    http_req_duration: ['p(95)<800'], // 95% 응답시간 < 800ms
  },
};

// UUID v4 생성기 (순수 UUID 형태 유지)
function uuidv4() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0;
    const v = c == 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

function uuidIdemv4() {
  return '7a2fbf84-065b-4a3d-9587-5133ce0b1cf6';
}

export default function () {
  const url = 'http://localhost:8081/api/orders';

  const idempotencyKey = uuidv4();
  const apiKey = `${TEST_RUN_ID}-${__VU}-${__ITER}-${uuidv4()}`;

  const payload = {
    idempotencyKey: idempotencyKey,
    apiKey: apiKey,
    productDesc: 'Example product description1',
    amount: 300.99,
    amountTaxFree: 29999,
  };

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(url, JSON.stringify(payload), params);

  // -------------------------------
  //  요청별 응답 출력 (상태코드 + body)
  // -------------------------------
//  console.log(
//    `[ITER ${__ITER}] status=${res.status} body=${res.body}`
//  );

  check(res, {
    'status is 2xx or 3xx': (r) => r.status >= 200 && r.status < 400,
  });
}