<!-- chapter: 24 | part: III | owner: writer-frontend | solutions -->
# Chapter 24 solutions

### Exercise 24.1

Add inside the `describe` block:

   ```typescript
   it('is active when only a sixth of a long timeout has passed', () => {
     expect(idleState(t0 + 100_000, t0, 600)).toEqual({ kind: 'active' });
   });
   ```

   With 600 seconds of timeout, 500 seconds are left; the warning window is the smaller of 300 seconds and half the timeout (300), so 500 is outside it. Run with `npm test` in `frontend/`.

### Exercise 24.2

`expectOne` fails the test, reporting that more than one matching request was found. It asserts *exactly* one, so it also catches accidental duplicate requests.

### Exercise 24.3

Example:

   ```typescript
   it('is signed in after login', () => {
     const session = TestBed.inject(SessionService);
     session.login('someone', 'pw').subscribe();
     TestBed.inject(HttpTestingController).expectOne('/api/auth/login')
       .flush({ username: 'someone', role: 'READER', sessionTimeoutSeconds: 1800, mustChangePassword: false });
     expect(session.isLoggedIn()).toBe(true);
   });
   ```

   Use the same `TestBed.configureTestingModule` providers as in Listing 24.2.

### Exercise 24.4

The two themes use different color values (Listing 21.5), so text that has enough contrast in one palette may not in the other. Checking only the light theme would leave the dark palette unverified.

### Exercise 24.5

The bug was in how nginx built the `X-Forwarded-For` header before passing the request to the backend, and the backend's decision to trust it. A Vitest spec runs neither. Only a test that sends real requests through the whole stack can see the header being overwritten (or not). A unit test could still add a check on the frontend side that no code sets that header itself, but that is not where the vulnerability was.
