import http from 'k6/http';
import {check, sleep} from 'k6';
import {uuidv4} from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const CONCERT_ID = 1;
const SEAT_ID = 1; // 경합 좌석

export const options = {
    setupTimeout: '180s',   // 3분까지 허용
    // 여러 시나리오 동시 구동
    scenarios: {
        // B: 대기열 토큰 발급 + 순번 조회
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
        // // A: 동일 좌석 점유 경합
        // seat_hold_race: {
        //     executor: 'ramping-arrival-rate',
        //     startRate: 50, timeUnit: '1s',
        //     preAllocatedVUs: 100, maxVUs: 500,
        //     stages: [
        //         { target: 200, duration: '30s' },
        //         { target: 400, duration: '30s' },
        //         { target: 0,   duration: '10s' },
        //     ],
        //     exec: 'seatHoldRace',
        //     tags: { api: 'seat_hold' },
        // },
        // // D: 좌석 목록 조회 폭주
        // seat_list_read: {
        //     executor: 'ramping-vus',
        //     startVUs: 10, stages: [
        //         { duration: '20s', target: 100 },
        //         { duration: '30s', target: 300 },
        //         { duration: '20s', target: 0 },
        //     ],
        //     exec: 'listSeats',
        //     tags: { api: 'list_seats' },
        // },
        // // C: 잔액 충전/예약 확정 혼합 (동일 유저 경합)
        // balance_race: {
        //     executor: 'per-vu-iterations',
        //     vus: 50, iterations: 10, maxDuration: '2m',
        //     exec: 'balanceRace',
        //     tags: { api: 'balance_reservation' },
        // },
    },
    thresholds: {
        // 'http_req_failed{api:seat_hold}':     ['rate<0.02'],
        // 'http_req_duration{api:seat_hold}':   ['p(95)<300'],
        'http_req_failed{api:queue}':         ['rate<0.02'],
        'http_req_duration{api:queue}':       ['p(95)<250'],
        // 'http_req_failed{api:list_seats}':    ['rate<0.01'],
        // 'http_req_duration{api:list_seats}':  ['p(95)<150'],
    },
};

// 공통 유틸
function json(res) {
    try { return res.json(); } catch { return {}; }
}

function hdrUser(userId) {
    return { headers: { 'Content-Type': 'application/json', 'User-Id': String(userId) } };
}

function hdrToken(token) {
    return { headers: { 'Content-Type': 'application/json', 'Queue-Token': String(token) } };
}

export function setup() {
    // 예: 테스트에 필요한 사용자 1000명과 콘서트 좌석 생성
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

// 시나리오 B: 대기열 토큰 발급 + 순번 조회
export function queueFlow() {
    const userId = Math.floor(Math.random() * 1000) + 1;
    // 토큰 발급
    const issue = http.post(`${BASE}/api/v1/queue/token`, null, hdrUser(userId));
    check(issue, { 'queue issue ok': (r) => [200, 201].includes(r.status) });
}

// 시나리오 C: 잔액 충전/예약 확정 혼합 (동일 userId 집중)
export function balanceRace() {
    const userId = 42; // 동일 유저에 경합 유발
    // 충전
    const chargeBody = JSON.stringify({ amount: 1000 });
    const charge = http.post(`${BASE}/api/v1/balance/charge`, chargeBody, hdrUser(userId));
    check(charge, { 'charge ok': (r) => [200, 409, 400].includes(r.status) });

    // 토큰 발급
    const issue = http.post(`${BASE}/api/v1/queue/tokens`, null, hdrUser(userId));

    // 토큰 확인 (간헐)
    if (Math.random() < 0.5) {
        const token = json(issue).data?.token || json(issue).token;
        if (token) {
            // 선행 점유
            const seatHoldUuid = uuidv4()
            const body = JSON.stringify({ seatHoldUuid: seatHoldUuid, concertId: CONCERT_ID, seatId: SEAT_ID });
            const hold = http.post(`${BASE}/api/v1/reservations/concerts/${CONCERT_ID}/seats/hold`, body, hdrToken(token));

            // 예약 확정
            const seatId = SEAT_ID;
            const confirmBody = JSON.stringify({ reservationUuid: uuidv4(), seatId, seatHoldUuid: seatHoldUuid });
            const confirm = http.post(`${BASE}/api/v1/reservations`, confirmBody, hdrToken(token));
            check(confirm, { 'confirm ok': (r) => [200, 400, 409].includes(r.status) });

            sleep(0.2);
        }
    }
}

// 시나리오 A: 동일 좌석 점유 경합
export function seatHoldRace() {
    // 토큰 발급
    const issue = http.post(`${BASE}/api/v1/queue/tokens`, null, hdrUser(userId));

    // 토큰 확인 (간헐)
    if (Math.random() < 0.5) {
        const token = json(issue).data?.token || json(issue).token;
        if (token) {
            const body = JSON.stringify({ seatHoldUuid: uuidv4(), concertId: CONCERT_ID, seatId: SEAT_ID });
            const res = http.post(`${BASE}/api/v1/reservations/concerts/${CONCERT_ID}/seats/hold`, body, hdrToken(token));

            check(res, {
                'hold: 200 or 409': (r) => [200, 409, 400].includes(r.status),
            });
        }
    }

}


// 시나리오 D: 좌석 목록 조회
export function listSeats() {
    // 토큰 발급
    const issue = http.post(`${BASE}/api/v1/queue/tokens`, null, hdrUser(userId));

    // 토큰 확인 (간헐)
    if (Math.random() < 0.5) {
        const token = json(issue).data?.token || json(issue).token;
        if (token) {
            const res = http.get(`${BASE}/api/v1/reservations/concerts/${CONCERT_ID}/seats`, hdrToken(token));
            check(res, { 'list ok': (r) => r.status === 200 });
        }
    }

}
