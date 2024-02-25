# react-native-ts-download-manager

React Native Download Manager

This package provides functionality to save files in your device's memory using the React Native framework. It also includes the capability to track download status through notifications.

## Installation

```sh

npm install react-native-ts-download-manager

OR

yarn add react-native-ts-download-manager

```

## Usage

```ts

import {
  download,
  queueDownload,
  attachOnCompleteListener,
  cancel,
  checkStatus
} from 'react-native-ts-download-manager';

// Example Usage

// Download a file
const downloadFile = async (): Promise<void> => {
  try {
    const url: string = 'https://example.com/file.zip';
    const headers: Record<string, string> = { Authorization: 'Bearer YOUR_ACCESS_TOKEN' };
    const config: { saveAsName?: string } = { saveAsName: 'MyDownloadedFile.zip' };

    const downloadResult = await download(url, headers, config);
    console.log('Download successful:', downloadResult);
  } catch (error) {
    console.error('Download error:', error);
  }
};

// Queue a download
const queueDownloadFile = async (): Promise<void> => {
  try {
    const url: string = 'https://example.com/document.pdf';
    const headers: Record<string, string> = { Authorization: 'Bearer YOUR_ACCESS_TOKEN' };
    const config: { saveAsName?: string } = { saveAsName: 'MyQueuedDocument.pdf' };

    const downloadId: string = await queueDownload(url, headers, config);
    console.log('Download queued with ID:', downloadId);
  } catch (error) {
    console.error('Queue download error:', error);
  }
};

// Attach onComplete listener
const attachCompleteListener = async (): Promise<void> => {
  try {
    const downloadId: string = '12345'; // Replace with your actual download ID
    const onCompleteResult = await attachOnCompleteListener(downloadId);
    console.log('OnComplete listener attached:', onCompleteResult);
  } catch (error) {
    console.error('Attach onComplete listener error:', error);
  }
};

// Cancel a download
const cancelDownload = async (): Promise<void> => {
  try {
    const downloadId: string = '12345'; // Replace with your actual download ID
    const cancelResult = await cancel(downloadId);
    console.log('Download canceled:', cancelResult);
  } catch (error) {
    console.error('Cancel download error:', error);
  }
};

// Check download status
const checkDownloadStatus = async (): Promise<void> => {
  try {
    const downloadId: string = '12345'; // Replace with your actual download ID
    const statusResult = await checkStatus(downloadId);
    console.log('Download status:', statusResult);
  } catch (error) {
    console.error('Check download status error:', error);
  }
};


```

## Contributing

Configuration
getRequestConfig

You can customize the download request configuration using the getRequestConfig function. It takes a configuration object and the download URL, and returns a merged configuration object.

```ts

import { getRequestConfig, Config } from 'react-native-ts-download-manager';

const customConfig: Config = getRequestConfig({
  downloadTitle: 'Custom Download',
  allowedInRoaming: false,
  external: true,
}, 'https://example.com/file.txt');

console.log('Merged Configuration:', customConfig);

```
Adjust the configuration options according to your requirements.

## License

License

This package is licensed under the [MIT License](https://github.com/aliomid1/react-native-ts-download-manager/LICENSE).

Feel free to contribute and report issues on the [GitHub repository](https://github.com/aliomid1/react-native-ts-download-manager).
