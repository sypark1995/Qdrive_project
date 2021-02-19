package com.giosis.util.qdrive.barcodescanner;

import com.google.zxing.BarcodeFormat;

import java.util.Vector;

@Deprecated
// 스캔가능한 format 저장
final class DecodeFormatManager {

    static final Vector<BarcodeFormat> ONE_D_FORMATS;
    static final Vector<BarcodeFormat> QR_CODE_FORMATS;
    static final Vector<BarcodeFormat> DATA_MATRIX_FORMATS;

    static {

        ONE_D_FORMATS = new Vector<>(9);
        ONE_D_FORMATS.add(BarcodeFormat.UPC_A);
        ONE_D_FORMATS.add(BarcodeFormat.UPC_E);
        ONE_D_FORMATS.add(BarcodeFormat.EAN_13);
        ONE_D_FORMATS.add(BarcodeFormat.EAN_8);
        ONE_D_FORMATS.add(BarcodeFormat.RSS_14);
        ONE_D_FORMATS.add(BarcodeFormat.CODE_39);
        ONE_D_FORMATS.add(BarcodeFormat.CODE_93);
        ONE_D_FORMATS.add(BarcodeFormat.CODE_128);
        ONE_D_FORMATS.add(BarcodeFormat.ITF);

        QR_CODE_FORMATS = new Vector<>(1);
        QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE);

        DATA_MATRIX_FORMATS = new Vector<>(1);
        DATA_MATRIX_FORMATS.add(BarcodeFormat.DATA_MATRIX);
    }
}