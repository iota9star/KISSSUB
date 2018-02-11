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
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import static com.afollestad.aesthetic.Rx.onErrorLogAndRethrow;
import static com.afollestad.aesthetic.Util.resolveResId;

/**
 * @author Aidan Follestad (afollestad)
 */
public class AestheticRadioButton extends AppCompatRadioButton {

    private CompositeDisposable compositeDisposable;
    private int backgroundResId;

    public AestheticRadioButton(Context context) {
        super(context);
    }

    public AestheticRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AestheticRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            backgroundResId = resolveResId(context, attrs, android.R.attr.background);
        }
    }

    private void invalidateColors(ColorIsDarkState state) {
        TintHelper.setTint(this, state.color(), state.isDark());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        compositeDisposable = new CompositeDisposable();
        Observable<Integer> obs = ViewUtil.getObservableForResId(
                getContext(), backgroundResId, Aesthetic.get(getContext()).colorAccent());
        if (obs != null) {
            compositeDisposable.add(
                    Observable.combineLatest(
                            obs,
                            Aesthetic.get(getContext()).isDark(),
                            ColorIsDarkState.creator())
                            .compose(Rx.distinctToMainThread())
                            .subscribe(this::invalidateColors, onErrorLogAndRethrow()));
        }
        compositeDisposable.add(
                Aesthetic.get(getContext())
                        .textColorPrimary()
                        .compose(Rx.distinctToMainThread())
                        .subscribe(ViewTextColorAction.create(this)));
    }

    @Override
    protected void onDetachedFromWindow() {
        compositeDisposable.clear();
        super.onDetachedFromWindow();
    }
}
