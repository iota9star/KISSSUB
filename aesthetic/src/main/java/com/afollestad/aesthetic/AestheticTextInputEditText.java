/*
 *
 *  *    Copyright 2018. iota9star
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.afollestad.aesthetic;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

import static com.afollestad.aesthetic.Rx.onErrorLogAndRethrow;
import static com.afollestad.aesthetic.Util.resolveResId;

/**
 * @author Aidan Follestad (afollestad)
 */
public class AestheticTextInputEditText extends TextInputEditText {

    private CompositeDisposable subs;
    private int backgroundResId;
    private ColorIsDarkState lastState;

    public AestheticTextInputEditText(Context context) {
        super(context);
    }

    public AestheticTextInputEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AestheticTextInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            backgroundResId = resolveResId(context, attrs, android.R.attr.background);
        }
    }

    private void invalidateColors(ColorIsDarkState state) {
        this.lastState = state;
        TintHelper.setTintAuto(this, state.color(), true, state.isDark());
        TintHelper.setCursorTint(this, state.color());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        subs = new CompositeDisposable();
        subs.add(
                Aesthetic.get(getContext())
                        .textColorPrimary()
                        .compose(Rx.<Integer>distinctToMainThread())
                        .subscribe(ViewTextColorAction.create(this), onErrorLogAndRethrow()));
        subs.add(
                Aesthetic.get(getContext())
                        .textColorSecondary()
                        .compose(Rx.<Integer>distinctToMainThread())
                        .subscribe(ViewHintTextColorAction.create(this), onErrorLogAndRethrow()));
        //noinspection ConstantConditions
        subs.add(
                Observable.combineLatest(
                        ViewUtil.getObservableForResId(
                                getContext(), backgroundResId, Aesthetic.get(getContext()).colorAccent()),
                        Aesthetic.get(getContext()).isDark(),
                        ColorIsDarkState.creator())
                        .compose(Rx.<ColorIsDarkState>distinctToMainThread())
                        .subscribe(
                                new Consumer<ColorIsDarkState>() {
                                    @Override
                                    public void accept(@NonNull ColorIsDarkState colorIsDarkState) {
                                        invalidateColors(colorIsDarkState);
                                    }
                                },
                                onErrorLogAndRethrow()));
    }

    @Override
    protected void onDetachedFromWindow() {
        subs.clear();
        super.onDetachedFromWindow();
    }

    @Override
    public void refreshDrawableState() {
        super.refreshDrawableState();
        if (lastState != null) {
            post(
                    new Runnable() {
                        @Override
                        public void run() {
                            invalidateColors(lastState);
                        }
                    });
        }
    }
}