import http from 'k6/http';
import {check} from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const CONCERT_ID = 1;
const SEAT_ID = 1; // 경합 좌석

export const options = {
    setupTimeout: '180s',   // 3분까지 허용

    scenarios: {
        // A : 대기열 토큰 발급 + 순번 조회
        queue_admission: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 200 },   // 30초 동안 200 VU로 증가
                { duration: '1m', target: 800 },    // 1분 동안 800 VU까지 증가 (피크)
                { duration: '30s', target: 800 },   // 800 VU 유지
                { duration: '1m', target: 0 },      // 1분 동안 종료
            ],
            exec: 'queueFlow',
            tags: { api: 'queue' },

        },
    },
    thresholds: {
        'http_req_duration{api:queue}': [ 'p(90)<1000', 'p(95)<1000', 'max<2000' ],
        'http_req_failed{api:queue}':  [ 'rate<0.01' ],
    },
    summaryTrendStats: ['avg','min','med','p(75)','p(90)','p(95)','p(99)','max'],
};

// 공통 유틸
function json(res) {
    try { return res.json(); } catch { return {}; }
}

function hdrUser(userId) {
    return { headers: { 'Content-Type': 'application/json', 'User-Id': String(userId) } };
}

export function setup() {
    // 테스트에 필요한 사용자 1000명과 콘서트 좌석 생성
    for (let i = 1; i <= 1000; i++) {
        http.post('http://localhost:8080/api/v1/users', JSON.stringify({
            balance: 200000,
        }), {headers: {'Content-Type': 'application/json'}});
    }
    
    http.post('http://localhost:8080/api/v1/reservations/concerts/for-test', null, {headers: {'Content-Type': 'application/json'}});
    
    for (let i = 1; i <= 500; i++) {
        http.post('http://localhost:8080/api/v1/reservations/concerts/seats', JSON.stringify({
            seatNumber: i,
        }), {headers: {'Content-Type': 'application/json'}});
    }
    
    return {ready: true}; // 이후 exec 함수들에서 접근 가능
}

// 시나리오 A: 대기열 토큰 발급 + 순번 조회
export function queueFlow() {
    const userId = Math.floor(Math.random() * 1000) + 1;
    // 토큰 발급
    const issue = http.post(`${BASE}/api/v1/queue/token`, null, hdrUser(userId));
    check(issue, { 'queue issue ok': (r) => [200, 201].includes(r.status) });
}
