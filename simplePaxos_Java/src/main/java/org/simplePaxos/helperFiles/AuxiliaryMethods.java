package org.simplePaxos.helperFiles;

import io.netty.buffer.ByteBuf;

public class AuxiliaryMethods {

    public static String readString(ByteBuf byteBuf){
        int size = byteBuf.readInt();
        byte [] bytes = new byte[size];
        byteBuf.readBytes(bytes);
        return new String(bytes);
    }

    public static void writeString(String content,ByteBuf byteBuf){
        byteBuf.writeInt(content.length());
        byteBuf.writeBytes(content.getBytes());
    }
}
