import React, { useEffect } from 'react';
import { Dimensions, Button, View, StyleSheet } from 'react-native';
import { VideoEffectView, videoEffect } from 'react-native-video-effect';

const { width, height } = Dimensions.get('window');

export default function App() {
  const [url, setUrl] = React.useState<string>('');

  const addTwoNumbers = async () => {
    try {
      const result = await videoEffect.convertImageToMat(
        'https://t3.ftcdn.net/jpg/05/54/16/34/360_F_554163402_HQoSz6uK3a6O4NgLzHKIFkxJztbOunlf.jpg'
      );
      console.log(result);
      setUrl(result);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    addTwoNumbers();
  }, [url]);

  return (
    <View style={styles.container}>
      <VideoEffectView style={styles.box} />
      {/* {url ? (
        <Image
          source={{ uri: `file:// ${url}` }}
          style={styles.image}
        />
      ) : null} */}
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
    height: height * 0.8,
    marginVertical: 20,
  },
  image: {
    width: 300,
    height: 300,
  },
});
