package com.androiddeveloper.chat.utils;

import android.media.AudioFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Pcm2Wav {
    /**
     * pac转wav
     *
     * @param pcmFile
     * @param wavfile
     * @param sampleRateInHz AudioFormat.ENCODING_PCM_16BIT
     * @param audioFormat    AudioFormat.ENCODING_PCM_16BIT
     * @param channels       声道数
     * @param bufferSize
     */
    public static void convert(
            File pcmFile, File wavfile, long sampleRateInHz, int audioFormat,
            int channels, int bufferSize) {
        long totalAudioLen;
        long totalDataLen;
        long byteRate = 16 * sampleRateInHz * channels / 8;
        if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
            byteRate = 16 * sampleRateInHz * channels / 8;
        } else if (audioFormat == AudioFormat.ENCODING_PCM_8BIT) {
            byteRate = 8 * sampleRateInHz * channels / 8;
        }
        byte[] data = new byte[bufferSize];
        try {
            FileInputStream in = new FileInputStream(pcmFile);
            FileOutputStream out = new FileOutputStream(wavfile);
            totalAudioLen = in.getChannel().size();
            //由于不包括前面的8个字节RIFF和WAV
            totalDataLen = totalAudioLen + 36;
            addWaveFileHeader(out, totalAudioLen, totalDataLen, sampleRateInHz, channels,
                    audioFormat, byteRate);
            while (in.read(data) != -1)
                out.write(data);
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //添加Wav头部信息
    private static void addWaveFileHeader(
            FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate,
            int channels, int audioFormat, long byteRate) throws IOException {
        byte[] header = new byte[44];
        // RIFF 头表示
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        //数据大小，真正大小是添加了8bit
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        //wave格式
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //fmt Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        if (audioFormat == AudioFormat.ENCODING_PCM_16BIT)
            header[32] = (byte) (channels * 16 / 8);
        else if (audioFormat == AudioFormat.ENCODING_PCM_8BIT)
            header[32] = (byte) (channels * 8 / 8);
        header[33] = 0;
        //每个样本的数据位数
        if (audioFormat == AudioFormat.ENCODING_PCM_16BIT)
            header[34] = 16;
        else if (audioFormat == AudioFormat.ENCODING_PCM_8BIT)
            header[34] = 8;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

}
