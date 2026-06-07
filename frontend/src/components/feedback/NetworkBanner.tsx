import { useNetworkStatus } from '../../lib/offline/network';

export function NetworkBanner() {
  const { isOnline, label } = useNetworkStatus();

  return <div className={`network-banner ${isOnline ? 'online' : 'offline'}`}>{label}</div>;
}
