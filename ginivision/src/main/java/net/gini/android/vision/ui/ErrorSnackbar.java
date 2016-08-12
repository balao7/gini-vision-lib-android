package net.gini.android.vision.ui;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.gini.android.vision.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @exclude
 */
public class ErrorSnackbar extends RelativeLayout {

    public static final int LENGTH_SHORT = 2000;
    public static final int LENGTH_LONG = 4000;
    public static final int LENGTH_INDEFINITE = Integer.MAX_VALUE;

    @VisibleForTesting
    static final int ANIM_DURATION = 250;
    private static final String TAG_SNACKBAR_ERROR = "GV_SNACKBAR_ERROR";

    private enum State {
        SHOWING, SHOWN,
        HIDING, HIDDEN
    }

    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private State mState = State.HIDDEN;
    private RelativeLayout mParentView;
    private int mShowDuration;
    private boolean mIsAttachedToWindow;
    private boolean mWaitForExisting = false;

    private TextView mTextView;
    private Button mButton;

    public static ErrorSnackbar make(@NonNull Context context,
                                     @NonNull RelativeLayout parentView,
                                     @NonNull String message,
                                     @Nullable String buttonTitle,
                                     @Nullable OnClickListener onClickListener,
                                     int duration) {
        ErrorSnackbar errorSnackbar = new ErrorSnackbar(context);
        errorSnackbar.setParentView(parentView);
        errorSnackbar.setMessage(message);
        errorSnackbar.setButtonTitle(buttonTitle);
        errorSnackbar.setButtonOnClickListener(onClickListener);
        errorSnackbar.setShowDuration(duration);
        return errorSnackbar;
    }

    public static void hideExisting(@NonNull RelativeLayout parentView) {
        removeExistingSnackbarsFromParentView(parentView);
    }

    public ErrorSnackbar(Context context) {
        super(context);
        init();
    }

    public ErrorSnackbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ErrorSnackbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.gv_layout_snackbar_error, this);
        setTag(TAG_SNACKBAR_ERROR);
        bindViews();
    }

    private void bindViews() {
        mTextView = (TextView) findViewById(R.id.gv_text_error);
        mButton = (Button) findViewById(R.id.gv_button_error);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttachedToWindow = false;
    }

    private void setMessage(@NonNull String text) {
        mTextView.setText(text);
    }

    private void setButtonTitle(@Nullable String title) {
        if (TextUtils.isEmpty(title)) {
            return;
        }
        mButton.setVisibility(View.VISIBLE);
        mButton.setText(title);
    }

    private void setButtonOnClickListener(@Nullable final OnClickListener onClickListener) {
        if (onClickListener == null) {
            return;
        }
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                onClickListener.onClick(v);
            }
        });
    }

    private void setShowDuration(int showDuration) {
        mShowDuration = showDuration;
    }

    private void setParentView(@NonNull RelativeLayout parentView) {
        // Parent view cannot be changed (acts as final)
        if (mParentView != null) {
            return;
        }
        mParentView = parentView;
        // Remove existing snackbars from the parent
        int removed = removeExistingSnackbarsFromParentView(mParentView);
        mWaitForExisting = removed > 0;
    }

    private int removeExistingSnackbarsFromParentView() {
        if (mParentView != null) {
            return removeExistingSnackbarsFromParentView(mParentView);
        }
        return 0;
    }

    private void addToParentView() {
        mParentView.addView(this);
    }

    private void removeFromParentView() {
        if (mParentView != null) {
            mParentView.removeView(this);
        }
        removeHandlerCallbacks(mHideRunnable);
    }

    private void removeHandlerCallbacks(@NonNull Runnable runnable) {
        if (!mIsAttachedToWindow) {
            return;
        }
        getHandler().removeCallbacks(runnable);
    }

    private static int removeExistingSnackbarsFromParentView(@NonNull RelativeLayout parentView) {
        List<ErrorSnackbar> existingSnackbars = getExistingSnackbarsFromParentView(parentView);
        for (ErrorSnackbar existingSnackbar : existingSnackbars) {
            existingSnackbar.hide();
        }
        return existingSnackbars.size();
    }

    @NonNull
    private static List<ErrorSnackbar> getExistingSnackbarsFromParentView(@NonNull RelativeLayout parentView) {
        List<ErrorSnackbar> existingSnackbars = new ArrayList<>();
        int childCount = parentView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parentView.getChildAt(i);
            Object tag = child.getTag();
            if (tag != null && tag.equals(TAG_SNACKBAR_ERROR) &&
                    child instanceof ErrorSnackbar) {
                existingSnackbars.add((ErrorSnackbar) child);
            }
        }
        return existingSnackbars;
    }

    public void show() {
        if (mState == State.SHOWING || mState == State.SHOWN) {
            return;
        }
        mState = State.SHOWING;

        addToParentView();
        setVisibility(View.INVISIBLE);
        // Delay to run after it has been measured and layouted
        post(new Runnable() {
            @Override
            public void run() {
                setTranslationY(getHeight());
                setVisibility(View.VISIBLE);

                animate()
                        .setStartDelay(mWaitForExisting ? ANIM_DURATION : 0)
                        .setDuration(ANIM_DURATION)
                        .translationY(0)
                        .setListener(new AnimatorListenerNoOp() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mState = State.SHOWN;
                                postHideRunnable();
                            }
                        });
            }
        });
    }

    private void postHideRunnable() {
        if (mShowDuration == LENGTH_INDEFINITE) {
            return;
        }
        removeHandlerCallbacks(mHideRunnable);
        postToHandlerDelayed(mHideRunnable, mShowDuration);
    }

    private void postToHandlerDelayed(@NonNull Runnable runnable, int duration) {
        if (!mIsAttachedToWindow) {
            return;
        }
        getHandler().postDelayed(runnable, duration);
    }

    public void hide() {
        if (mState == State.HIDING || mState == State.HIDDEN) {
            return;
        }
        mState = State.HIDING;

        removeHandlerCallbacks(mHideRunnable);

        animate()
                .setDuration(ANIM_DURATION)
                .translationY(getHeight())
                .setListener(new AnimatorListenerNoOp() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mState = State.HIDDEN;
                        removeFromParentView();
                    }
                });
    }
}
