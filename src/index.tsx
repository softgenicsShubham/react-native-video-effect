import React, { useEffect } from 'react';
import { DeviceEventEmitter } from 'react-native';
import {
  requireNativeComponent,
  UIManager,
  NativeModules,
  type ViewStyle,
} from 'react-native';

const LINKING_ERROR =
  'You have not properly linked the native module or you are running in the Expo client.';

type VideoEffectProps = {
  style: ViewStyle;
};

const ComponentName = 'VideoEffectView';

const VideoEffectViewComponent =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<VideoEffectProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

export const VideoEffectView = (props: VideoEffectProps) => {
  useEffect(() => {
    const subscription = DeviceEventEmitter.addListener(
      'ImageDetailsEvent',
      function (e: any) {
        console.log('Image details: ', e.width, e.height, e.planes);
      }
    );

    return () => {
      subscription.remove();
    };
  }, []);

  return <VideoEffectViewComponent {...props} />;
};

export const videoEffect = NativeModules.VideoEffectModules;
