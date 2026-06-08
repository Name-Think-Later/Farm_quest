import { useNetworkStatus } from '../../lib/offline/network';

export function NetworkBanner() {
  const { isOnline, label } = useNetworkStatus();

  if (isOnline || !label) {
    return null;
  }

  return <div className="network-banner offline">{label}</div>;
}
