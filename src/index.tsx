import {
  requireNativeComponent,
  UIManager,
  Platform,
  NativeModules,
  type ViewStyle,
} from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-video-effect' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

type VideoEffectProps = {
  // color: string;
  style: ViewStyle;
};

const ComponentName = 'VideoEffectView';

export const VideoEffectView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<VideoEffectProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

export const videoEffect = NativeModules.VideoEffectModules;
