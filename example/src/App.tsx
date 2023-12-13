import * as React from 'react';

import { Button, Dimensions, StyleSheet, View } from 'react-native';
import { VideoEffectView, videoEffect } from 'react-native-video-effect';

const { width, height } = Dimensions.get('window');

export default function App() {
  const addTwoNumbers = async () => {
    try {
      const result = await videoEffect.generateRandomMatrix(5, 5);
      console.log(result);
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <View style={styles.container}>
      <VideoEffectView style={styles.box} />
      <Button title="Add two numbers" onPress={addTwoNumbers} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: width,
    height: height,
    marginVertical: 20,
  },
});
