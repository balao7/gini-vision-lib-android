package net.gini.android.vision.internal.camera.photo;

import static org.apache.commons.imaging.Imaging.getMetadata;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;

import java.io.IOException;
import java.util.Arrays;

/**
 * @exclude
 */
class ExifReader {

    private final JpegImageMetadata mJpegMetadata;

    static ExifReader forJpeg(@NonNull final byte[] jpeg) {
        try {
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) getMetadata(jpeg);
            if (jpegMetadata == null) {
                throw new ExifReaderException("No jpeg metadata found");
            }
            return new ExifReader(jpegMetadata);
        } catch (IOException | ImageReadException e) {
            throw new ExifReaderException("Could not read jpeg metadata: " + e.getMessage(), e);
        }
    }

    private ExifReader(@NonNull final JpegImageMetadata jpegMetadata) {
        mJpegMetadata = jpegMetadata;
    }

    @NonNull
    String getUserComment() {
        final TiffField userCommentField = mJpegMetadata.findEXIFValue(
                ExifTagConstants.EXIF_TAG_USER_COMMENT);
        if (userCommentField == null) {
            throw new ExifReaderException("No User Comment found");
        }

        final byte[] rawUserComment = userCommentField.getByteArrayValue();
        if (rawUserComment == null) {
            throw new ExifReaderException("No User Comment found");
        }

        if (rawUserComment.length >= 8) {
            return new String(Arrays.copyOfRange(rawUserComment, 8, rawUserComment.length));
        } else {
            return new String(rawUserComment);
        }
    }

    @Nullable
    String getValueForKeyFromUserComment(@NonNull final String key,
            @NonNull final String userComment) {
        final String[] keyValuePairs = userComment.split(",");
        for (final String keyValuePair : keyValuePairs) {
            final String[] keyAndValue = keyValuePair.split("=");
            if (keyAndValue.length > 1 && keyAndValue[0].equals(key)) {
                return keyAndValue[1];
            }
        }
        return null;
    }
}
