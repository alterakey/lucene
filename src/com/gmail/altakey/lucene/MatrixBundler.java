package com.gmail.altakey.lucene;

import android.graphics.Matrix;
import android.os.Parcel;
import android.os.Parcelable;

public class MatrixBundler implements Parcelable {
    private Matrix mMatrix;

    public MatrixBundler(final Matrix m) {
        mMatrix = m;
    }

    private MatrixBundler(final Parcel in) {
        final float[] values = new float[9];
        in.readFloatArray(values);

        mMatrix = new Matrix();
        mMatrix.setValues(values);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        final float[] values = new float[9];
        mMatrix.getValues(values);
        out.writeFloatArray(values);
    }

    public static final Parcelable.Creator<MatrixBundler> CREATOR = new Parcelable.Creator<MatrixBundler>() {
        public MatrixBundler createFromParcel(Parcel in) {
            return new MatrixBundler(in);
        }
        
        public MatrixBundler[] newArray(int size) {
            return new MatrixBundler[size];
        }
    };

    public Matrix getMatrix() {
        return mMatrix;
    }

    public void setMatrix(final Matrix matrix) {
        mMatrix = matrix;
    }
}
