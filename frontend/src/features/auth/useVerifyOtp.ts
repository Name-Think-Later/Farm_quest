import { useMutation } from '@tanstack/react-query';
import { verifyOtp } from './auth.api';
import { useSessionStore } from '../session/sessionStore';

export function useVerifyOtp() {
  const authenticate = useSessionStore((state) => state.authenticate);

  return useMutation({
    mutationFn: verifyOtp,
    onSuccess: (result) => {
      if (!result.ok || !result.data.sessionToken) {
        return;
      }

      authenticate(result.data.sessionToken, result.data.email);
    },
  });
}
