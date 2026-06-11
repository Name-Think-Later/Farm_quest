import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { LoadingState } from '../../components/feedback/LoadingState';
import { ErrorState } from '../../components/feedback/ErrorState';
import { useCurrentQuest, useLocationHint, useVerifyLocation } from '../../features/quests/useQuestFlows';
import type { LocationVerificationPayload } from '../../features/quests/types';

type GpsErrorType = 'none' | 'permission-denied' | 'low-accuracy' | 'wrong-location';

function getCurrentPosition(): Promise<GeolocationPosition> {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('目前瀏覽器不支援定位功能。'));
      return;
    }

    navigator.geolocation.getCurrentPosition(resolve, reject, {
      enableHighAccuracy: true,
      timeout: 10_000,
      maximumAge: 0,
    });
  });
}

export function LocationVerifyPage() {
  const navigate = useNavigate();
  const [gpsError, setGpsError] = useState<GpsErrorType>('none');
  const currentQuestQuery = useCurrentQuest();
  const questId = currentQuestQuery.data?.questId;
  const hintQuery = useLocationHint(questId);
  const mutation = useVerifyLocation(questId);

  const getGpsErrorMessage = (errorType: GpsErrorType): string | null => {
    switch (errorType) {
      case 'permission-denied':
        return 'GPS未授權。請在瀏覽器設定中允許定位權限。';
      case 'low-accuracy':
        return '精準度太低。請移至空曠處或稍後再試。';
      case 'wrong-location':
        return '位置錯誤。您尚未抵達正確的景點位置。';
      default:
        return null;
    }
  };

  return (
    <MobileShell
      title="地點驗證"
      actions={
        <button
          type="button"
          className="primary-button"
          onClick={async () => {
            setGpsError('none');
            let payload: LocationVerificationPayload;

            try {
              const position = await getCurrentPosition();
              payload = {
                permissionDenied: false,
                latitude: position.coords.latitude,
                longitude: position.coords.longitude,
                accuracyMeters: position.coords.accuracy,
              };
            } catch (error) {
              const permissionDenied = typeof error === 'object'
                && error !== null
                && 'code' in error
                && (error as { code?: number }).code === 1;

              if (permissionDenied) {
                setGpsError('permission-denied');
                payload = { permissionDenied: true };
              } else {
                throw error;
              }
            }

            const result = await mutation.mutateAsync(payload);
            if (result.passed) {
              navigate('/quest/riddle');
            } else {
              // 判断是精度问题还是位置问题
              if (hintQuery.data && result.accuracyMeters > hintQuery.data.maxAccuracyMeters) {
                setGpsError('low-accuracy');
              } else {
                setGpsError('wrong-location');
              }
            }
          }}
          disabled={mutation.isPending || currentQuestQuery.isLoading || hintQuery.isLoading || !questId}
        >
          {mutation.isPending ? '確認中…' : '已抵達，前往下一步'}
        </button>
      }
    >
      <NetworkBanner />
      {currentQuestQuery.isLoading || hintQuery.isLoading ? <LoadingState message="正在載入地點資訊…" /> : null}
      {currentQuestQuery.error ? <ErrorState message={(currentQuestQuery.error as Error).message} onRetry={() => void currentQuestQuery.refetch()} /> : null}
      {hintQuery.error ? <ErrorState message={(hintQuery.error as Error).message} onRetry={() => void hintQuery.refetch()} /> : null}
      {mutation.error ? <ErrorState message={(mutation.error as Error).message} /> : null}
      {gpsError !== 'none' ? (
        <div className="status-card status-error">
          <strong>定位錯誤</strong>
          <p>{getGpsErrorMessage(gpsError)}</p>
        </div>
      ) : null}
      <div className="section-card accent-card">
        <strong>任務提示</strong>
        <p>{hintQuery.data?.hintText ?? '確認已抵達景點後，按下按鈕即可前往下一步，開始與 AI NPC 對話。'}</p>
        {hintQuery.data ? (
          <p className="helper-text">
            地點：{hintQuery.data.locationName} · 驗證半徑 {hintQuery.data.radiusMeters} 公尺 · 最大允許精度 {hintQuery.data.maxAccuracyMeters} 公尺
          </p>
        ) : null}
      </div>
      {mutation.data && mutation.data.passed ? (
        <div className="status-card">
          <strong>驗證通過</strong>
          <p>距離任務點約 {Math.round(mutation.data.distanceMeters)} 公尺。</p>
          <p>目前定位精度 {Math.round(mutation.data.accuracyMeters)} 公尺。</p>
        </div>
      ) : null}
      <button type="button" className="text-button" onClick={() => navigate('/quest/current')}>
        返回任務頁
      </button>
    </MobileShell>
  );
}
