package com.example.nromantsov.myapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.view.SupportMenuInflater;
import android.util.AndroidRuntimeException;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * Created by n.romantsov on 27.03.2017.
 */

public class FabMenu extends FrameLayout implements View.OnClickListener {
    private static final int DEFAULT_MENU_ID = 0;
    private static final int DEFAULT_MENU_OFFSET = 150;
    private static final float DEFAULT_ANGLE_MENU = 90;
    private static final long ANIMATION_DURATION = 200;

    private FrameLayout menuItemsLayout;
    private NavigationMenu navigationMenu;
    private ColorStateList backgroundMenu;
    private int menuId;
    private float angleMenuItem;
    private float offset;
    private float offsetAnimation;
    private float margin_menu;
    private ArrayList<FloatingActionButton> listFabMenu;

    private View touchGuard = null;
    private boolean useTouchGuard;
    private Drawable touchGuardDrawable;

    private FloatingActionButton fab;
    private ColorStateList background;
    private ArrayList<Drawable> drawableFab;

    private ArrayList<AnimatorSet> mShowAnimation = new ArrayList<>();
    private ArrayList<AnimatorSet> mHideAnimation = new ArrayList<>();

    public FabMenu(@NonNull Context context) {
        super(context);
    }

    public FabMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FabMenu, 0, 0);
        resolveCompulsoryAttributes(typedArray);
        resolveOptionalAttributes(typedArray);
        typedArray.recycle();

        LayoutInflater.from(context).inflate(R.layout.fab_menu, this, true);
        menuItemsLayout = (FrameLayout) findViewById(R.id.menu_items_layout);
        newNavigationMenu();

        //Угол между под меню Fab'ов
        angleMenuItem = DEFAULT_ANGLE_MENU / (navigationMenu.size() - 1);
        offsetAnimation = getResources().getDimension(R.dimen.offset_animation);

        listFabMenu = new ArrayList<>();
        addMenuItems();
    }

    @Override
    public void onClick(View view) {

    }

    private void resolveCompulsoryAttributes(TypedArray typedArray) {
        if (typedArray.hasValue(R.styleable.FabMenu_fab_menu)) {
            menuId = typedArray.getResourceId(R.styleable.FabMenu_fab_menu, DEFAULT_MENU_ID);
        } else {
            throw new AndroidRuntimeException("You must provide the id of the menu resource.");
        }

        if (typedArray.hasValue(R.styleable.FabMenu_fab_menu_offset)) {
            offset = typedArray.getDimension(R.styleable.FabMenu_fab_menu_offset, DEFAULT_MENU_OFFSET);
        } else {
            throw new AndroidRuntimeException("You must provide the offset of the menu.");
        }

        if (typedArray.hasValue(R.styleable.FabMenu_fab_menu_offset)) {
            background = typedArray.getColorStateList(R.styleable.FabMenu_fab_background_tint);
        } else {
            throw new AndroidRuntimeException("You must provide the background of the menu.");
        }
    }

    private void resolveOptionalAttributes(TypedArray typedArray) {
        backgroundMenu = typedArray.getColorStateList(R.styleable.FabMenu_fab_menu_background_tint);

        margin_menu = typedArray.getDimension(R.styleable.FabMenu_layout_margin_menu, getResources().getDimension(R.dimen.fab_margin));

        //Загрузка картинок из ресурса array
//        TypedValue outValue = new TypedValue();
//        if (typedArray.getValue(R.styleable.FabMenu_menu_drawable, outValue)) {
//            TypedArray array = getResources().obtainTypedArray(outValue.resourceId);
//            drawableFab = new ArrayList<>();
//            for (int i = 0; i < array.length(); i++) {
//                TypedValue value = array.peekValue(i);
//                drawableFab.add(getResources().getDrawable(value != null ? value.resourceId : 0));
//            }
//            array.recycle();
//        }

        useTouchGuard = typedArray.getBoolean(R.styleable.FabMenu_touch_guard, false);
        touchGuardDrawable = typedArray.getDrawable(R.styleable.FabMenu_touch_guard_drawable);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setImageDrawable(drawableFab.get(0));
        fab.setBackgroundTintList(background);

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMenuOpen())
                    closeMenu();
                else
                    openMenu();
            }
        });

        // Needed in order to intercept key events
        setFocusableInTouchMode(true);

        if (useTouchGuard) {
            ViewParent parent = getParent();

            touchGuard = new View(getContext());
            touchGuard.setOnClickListener(this);
            touchGuard.setWillNotDraw(true);
            touchGuard.setVisibility(GONE);

            if (touchGuardDrawable != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    touchGuard.setBackground(touchGuardDrawable);
                } else {
                    touchGuard.setBackgroundDrawable(touchGuardDrawable);
                }
            }

            if (parent instanceof FrameLayout) {
                FrameLayout frameLayout = (FrameLayout) parent;
                frameLayout.addView(touchGuard);
                bringToFront();
            } else if (parent instanceof ConstraintLayout) {
                ConstraintLayout constraintLayout = (ConstraintLayout) parent;
                constraintLayout.addView(touchGuard);
                bringToFront();
            } else if (parent instanceof RelativeLayout) {
                RelativeLayout relativeLayout = (RelativeLayout) parent;
                relativeLayout.addView(touchGuard,
                        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                bringToFront();
            } else {
                throw new AndroidRuntimeException("TouchGuard requires that the parent of this FabMenu be a FrameLayout or RelativeLayout");
            }
        }

        setOnClickListener(this);
    }

    public void openMenu() {
        if (!ViewCompat.isAttachedToWindow(this))
            return;
        fab.setSelected(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            fab.setElevation(getResources().getDimension(R.dimen.elevation));

        if (touchGuard != null) touchGuard.setVisibility(VISIBLE);

        menuItemsLayout.setVisibility(VISIBLE);

        startShowAnimate();
    }

    private void addMenuItems() {
        for (int i = 0; i < navigationMenu.size(); i++) {
            menuItemsLayout.addView(createFabMenuItem(i));
        }
    }

    private View createFabMenuItem(int i) {
        ViewGroup fabMenuItem = (ViewGroup) LayoutInflater.from(getContext())
                .inflate(R.layout.fab_menu_item, this, false);

        FloatingActionButton miniFab = (FloatingActionButton) fabMenuItem.findViewById(R.id.mini_fab);

        int x0 = (int) (offset * Math.cos((i * angleMenuItem) * Math.PI / 180) + margin_menu);
        int y0 = (int) (offset * Math.sin((i * angleMenuItem) * Math.PI / 180) + margin_menu);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) miniFab.getLayoutParams();
        layoutParams.leftMargin = x0;
        layoutParams.topMargin = y0;
        miniFab.setLayoutParams(layoutParams);

//        miniFab.setImageDrawable(drawableFab.get(i + 1));
        miniFab.setBackgroundTintList(backgroundMenu == null ? background : backgroundMenu);
        miniFab.setOnClickListener(this);
        ViewCompat.setAlpha(miniFab, 1f);

        createAnimationFab(miniFab, i);
        listFabMenu.add(miniFab);

        return fabMenuItem;
    }

    private void createAnimationFab(FloatingActionButton miniFab, int i) {
        int x1 = (int) (offset * Math.cos((i * angleMenuItem) * Math.PI / 180));
        int y1 = (int) (offset * Math.sin((i * angleMenuItem) * Math.PI / 180));

        int x2 = (int) (offsetAnimation * Math.cos((i * angleMenuItem) * Math.PI / 180));
        int y2 = (int) (offsetAnimation * Math.sin((i * angleMenuItem) * Math.PI / 180));

        ObjectAnimator animShowBoundX = ObjectAnimator.ofFloat(miniFab, "translationX", -x1, x2);
        ObjectAnimator animShowBoundY = ObjectAnimator.ofFloat(miniFab, "translationY", -y1, y2);
        ObjectAnimator animShowAlpha = ObjectAnimator.ofInt(miniFab, "alpha", 0, 255);
        AnimatorSet animShowBoundXY = new AnimatorSet();
        animShowBoundXY.playTogether(animShowBoundX, animShowBoundY, animShowAlpha);
        animShowBoundXY.setDuration(ANIMATION_DURATION);
        animShowBoundXY.setInterpolator(new LinearInterpolator());
        mShowAnimation.add(animShowBoundXY);

        ObjectAnimator animShowX = animShowBoundX.clone();
        ObjectAnimator animShowY = animShowBoundY.clone();
        animShowX.setFloatValues(x2, 0);
        animShowY.setFloatValues(y2, 0);
        AnimatorSet animShowXY = new AnimatorSet();
        animShowXY.playTogether(animShowX, animShowY);
        animShowXY.setStartDelay(ANIMATION_DURATION);
        animShowXY.setDuration(ANIMATION_DURATION);
        animShowXY.setInterpolator(new AnticipateOvershootInterpolator());
        mShowAnimation.add(animShowXY);

        ObjectAnimator animHideX = animShowBoundX.clone();
        ObjectAnimator animHideY = animShowBoundY.clone();
        ObjectAnimator animHideAlpha = ObjectAnimator.ofInt(miniFab, "alpha", 255, 0);
        animHideX.setFloatValues(0f, -x1);
        animHideY.setFloatValues(0f, -y1);
        AnimatorSet animHideXY = new AnimatorSet();
        animHideXY.playTogether(animHideX, animHideY, animHideAlpha);
        animHideXY.setInterpolator(new LinearInterpolator());
        mHideAnimation.add(animHideXY);
    }

    public void closeMenu() {
        if (!ViewCompat.isAttachedToWindow(this))
            return;

        fab.setSelected(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            fab.setElevation(0);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (touchGuard != null) touchGuard.setVisibility(GONE);
                menuItemsLayout.setVisibility(GONE);
            }
        };

        postDelayed(runnable, ANIMATION_DURATION / 2);

        startHideAnimate();
    }

    private void newNavigationMenu() {
        navigationMenu = new NavigationMenu(getContext());
        new SupportMenuInflater(getContext()).inflate(menuId, navigationMenu);
    }

    public boolean isMenuOpen() {
        return menuItemsLayout.getVisibility() == VISIBLE;
    }

    private void startShowAnimate() {
        for (AnimatorSet objectAnimator : mShowAnimation) {
            objectAnimator.start();
        }
    }

    private void startHideAnimate() {
        for (AnimatorSet objectAnimator : mHideAnimation) {
            objectAnimator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHideAnimation.clear();
        mHideAnimation = null;
        mShowAnimation.clear();
        mHideAnimation = null;
    }
}