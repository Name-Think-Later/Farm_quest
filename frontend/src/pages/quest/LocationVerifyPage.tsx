import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { LoadingState } from '../../components/feedback/LoadingState';
import { ErrorState } from '../../components/feedback/ErrorState';
import { useCurrentQuest, useLocationHint, useVerifyLocation } from '../../features/quests/useQuestFlows';
import type { LocationVerificationPayload } from '../../features/quests/types';

type GpsErrorType = 'none' | 'permission-denied' | 'low-accuracy' | 'wrong-location' | 'not-supported' | 'timeout';

async function checkGeolocationPermission(): Promise<PermissionState> {
  if (!navigator.permissions) {
    // 如果浏览器不支持 Permissions API，返回 prompt 以触发请求
    return 'prompt';
  }
  try {
    const result = await navigator.permissions.query({ name: 'geolocation' });
    return result.state;
  } catch {
    return 'prompt';
  }
}

async function getCurrentPosition(): Promise<GeolocationPosition> {
  // 先检查权限状态
  const permissionState = await checkGeolocationPermission();

  if (permissionState === 'denied') {
    throw new Error('GPS_PERMISSION_DENIED');
  }

  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('GPS_NOT_SUPPORTED'));
      return;
    }

    navigator.geolocation.getCurrentPosition(resolve, (error) => {
      // 将 GeolocationPositionError 转换为可识别的错误
      const errorCode = error.code;
      const errorMessage = error.message;

      switch (errorCode) {
        case error.PERMISSION_DENIED:
          reject(new Error('GPS_PERMISSION_DENIED'));
          break;
        case error.POSITION_UNAVAILABLE:
          reject(new Error('GPS_POSITION_UNAVAILABLE'));
          break;
        case error.TIMEOUT:
          reject(new Error('GPS_TIMEOUT'));
          break;
        default:
          reject(new Error(`GPS_ERROR: ${errorMessage}`));
          break;
      }
    }, {
      enableHighAccuracy: true,
      timeout: 10_000,
      maximumAge: 0,
    });
  });
}

export function LocationVerifyPage() {
  const navigate = useNavigate();
  const [gpsError, setGpsError] = useState<GpsErrorType>('none');
  const [isCheckingPermission, setIsCheckingPermission] = useState(false);
  const [testMode, setTestMode] = useState(false);
  const [testLatitude, setTestLatitude] = useState('23.715');
  const [testLongitude, setTestLongitude] = useState('120.76');
  const [testAccuracy, setTestAccuracy] = useState('30');
  const currentQuestQuery = useCurrentQuest();
  const questId = currentQuestQuery.data?.questId;
  const hintQuery = useLocationHint(questId);
  const mutation = useVerifyLocation(questId);

  const getGpsErrorMessage = (errorType: GpsErrorType): string | null => {
    switch (errorType) {
      case 'permission-denied':
        return 'GPS未授權。請在瀏覽器設定中允許定位權限，或使用 HTTPS 協議訪問。';
      case 'not-supported':
        return '目前瀏覽器不支援定位功能。請使用支援 GPS 的瀏覽器。';
      case 'timeout':
        return '定位逾時。請移至空曠處或稍後再試。';
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
            setIsCheckingPermission(true);

            try {
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
                const errorMessage = error instanceof Error ? error.message : '';

                if (errorMessage === 'GPS_PERMISSION_DENIED') {
                  setGpsError('permission-denied');
                  payload = { permissionDenied: true };
                } else if (errorMessage === 'GPS_NOT_SUPPORTED') {
                  setGpsError('not-supported');
                  return; // 不支持GPS，直接返回
                } else if (errorMessage === 'GPS_TIMEOUT') {
                  setGpsError('timeout');
                  return; // 超时，直接返回
                } else if (errorMessage === 'GPS_POSITION_UNAVAILABLE') {
                  setGpsError('timeout'); // 使用timeout错误类型
                  return;
                } else {
                  // 其他错误，抛出
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
            } finally {
              setIsCheckingPermission(false);
            }
          }}
          disabled={isCheckingPermission || mutation.isPending || currentQuestQuery.isLoading || hintQuery.isLoading || !questId}
        >
          {isCheckingPermission ? '檢查權限中…' : mutation.isPending ? '確認中…' : '已抵達，前往下一步'}
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
      {/* 測試模式功能 - 暫時隱藏，需要時將 display: 'none' 改為 display: 'block' */}
      <div style={{ display: 'none' }}>
        <button
          type="button"
          className="text-button"
          onClick={() => setTestMode(!testMode)}
          style={{ marginTop: '8px' }}
        >
          {testMode ? '關閉測試模式' : '開啟測試模式（手動輸入坐標）'}
        </button>
        {testMode ? (
          <div className="section-card" style={{ marginTop: '16px' }}>
            <strong>測試模式</strong>
            <p className="helper-text">手動輸入GPS坐標以繞過真實定位驗證</p>
            <div style={{ marginTop: '12px' }}>
              <label style={{ display: 'block', marginBottom: '8px' }}>
                緯度：
                <input
                  type="text"
                  value={testLatitude}
                  onChange={(e) => setTestLatitude(e.target.value)}
                  style={{ marginLeft: '8px', padding: '4px', width: '120px' }}
                />
              </label>
              <label style={{ display: 'block', marginBottom: '8px' }}>
                經度：
                <input
                  type="text"
                  value={testLongitude}
                  onChange={(e) => setTestLongitude(e.target.value)}
                  style={{ marginLeft: '8px', padding: '4px', width: '120px' }}
                />
              </label>
              <label style={{ display: 'block', marginBottom: '8px' }}>
                精度（公尺）：
                <input
                  type="text"
                  value={testAccuracy}
                  onChange={(e) => setTestAccuracy(e.target.value)}
                  style={{ marginLeft: '8px', padding: '4px', width: '120px' }}
                />
              </label>
              <button
                type="button"
                className="primary-button"
                onClick={async () => {
                  setGpsError('none');
                  try {
                    const payload: LocationVerificationPayload = {
                      permissionDenied: false,
                      latitude: parseFloat(testLatitude),
                      longitude: parseFloat(testLongitude),
                      accuracyMeters: parseFloat(testAccuracy),
                    };
                    const result = await mutation.mutateAsync(payload);
                    if (result.passed) {
                      navigate('/quest/riddle');
                    } else {
                      if (hintQuery.data && result.accuracyMeters > hintQuery.data.maxAccuracyMeters) {
                        setGpsError('low-accuracy');
                      } else {
                        setGpsError('wrong-location');
                      }
                    }
                  } catch (error) {
                    console.error('Test mode error:', error);
                  }
                }}
                disabled={mutation.isPending || !questId}
                style={{ marginTop: '12px' }}
              >
                {mutation.isPending ? '確認中…' : '測試提交'}
              </button>
            </div>
          </div>
        ) : null}
      </div>
    </MobileShell>
  );
}
