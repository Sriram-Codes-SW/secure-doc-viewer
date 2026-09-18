import { idleState } from './idle';

describe('idleState', () => {
  const t0 = 1_000_000;

  it('is active well before the timeout', () => {
    expect(idleState(t0 + 10 * 60_000, t0, 1800)).toEqual({ kind: 'active' });
  });

  it('warns in the last five minutes, counting down', () => {
    expect(idleState(t0 + 26 * 60_000, t0, 1800)).toEqual({ kind: 'warning', secondsLeft: 240 });
  });

  it('expires once the timeout has passed', () => {
    expect(idleState(t0 + 30 * 60_000, t0, 1800)).toEqual({ kind: 'expired' });
  });

  it('scales the warning window down for short timeouts', () => {
    expect(idleState(t0 + 30_000, t0, 120)).toEqual({ kind: 'active' });
    expect(idleState(t0 + 70_000, t0, 120)).toEqual({ kind: 'warning', secondsLeft: 50 });
  });
});
