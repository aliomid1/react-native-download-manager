import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-ts-download-manager' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const DownloadManager = NativeModules.DownloadManager
  ? NativeModules.DownloadManager
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
export interface Config {
  downloadTitle?: string;
  downloadDescription?: string;
  saveAsName?: string;
  allowedInRoaming?: boolean;
  allowedInMetered?: boolean;
  showInDownloads?: boolean;
  external?: boolean;
  path?: string;
}

const getRequestConfig = (config: Config, url: string): Config => ({
  downloadTitle: 'File Download',
  downloadDescription: url,
  saveAsName: 'Downloaded File - ' + new Date(),
  allowedInRoaming: true,
  allowedInMetered: true,
  showInDownloads: true,
  external: false,
  path: "Download/",
  ...config,
});

const download = (url: string = '', headers: Record<string, string> = {}, config: Config = {}): Promise<any> => {
  const downloadRequestConfig = getRequestConfig(config, url);
  return new Promise((resolve, reject) => {
    DownloadManager.download(url, headers, downloadRequestConfig, (err: string | null, data: any) => {
      if (err) {
        return reject(err);
      }
      return resolve(data);
    });
  });
};

const queueDownload = (url: string = '', headers: Record<string, string> = {}, config: Config = {}): Promise<any> => {
  const downloadRequestConfig = getRequestConfig(config, url);
  return new Promise((resolve, reject) => {
    DownloadManager.queueDownload(url, headers, downloadRequestConfig, (err: string | null, data: any) => {
      if (err) {
        return reject(err);
      }
      return resolve(data);
    });
  });
};

const attachOnCompleteListener = (downloadId: string = ''): Promise<any> => new Promise((resolve, reject) => {
  DownloadManager.attachOnCompleteListener(downloadId, (err: string | null, data: any) => {
    if (err) {
      return reject(err);
    }
    return resolve(data);
  });
});

const cancel = (downloadId: string = ''): Promise<any> => new Promise((resolve, reject) => {
  DownloadManager.cancel(downloadId, (err: string | null, data: any) => {
    if (err) {
      return reject(err);
    }
    return resolve(data);
  });
});

const checkStatus = (downloadId: string = ''): Promise<any> => new Promise((resolve, reject) => {
  DownloadManager.checkStatus(downloadId, (err: string | null, data: any) => {
    if (err) {
      return reject(err);
    }
    return resolve(data);
  });
});

export {
  download,
  queueDownload,
  attachOnCompleteListener,
  cancel,
  checkStatus,
  getRequestConfig
};
