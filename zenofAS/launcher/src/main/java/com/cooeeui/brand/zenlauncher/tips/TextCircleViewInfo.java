package com.cooeeui.brand.zenlauncher.tips;

import com.cooeeui.zenlauncher.R;

public class TextCircleViewInfo {

    public static final int DAY = 1;
    public static final int WEEK = 2;
    public static final int MONTH = 3;
    public static final int YEAR = 4;

    // 次数为单位
    private long unlock_times;
    // 秒为单位
    private float phone_time;

    public int type = 1;

    int[] Colors = {
        R.color.circle_green, R.color.circle_blue, R.color.circle_yellow, R.color.circle_red
    };

    public long getUnlock_times() {
        return unlock_times;
    }

    public void setUnlock_times(long unlock_times) {
        this.unlock_times = unlock_times;
    }

    public float getPhone_time() {
        return phone_time;
    }

    public void setPhone_time(float phone_time) {
        this.phone_time = phone_time;
    }

    public float getPhone_timeWithMinute() {
        float f = (float) (Math.round((float) phone_time / 60 * 10)) / 10;
        return f;
    }

    private long unLockScores() {
        return getUnlock_times() * 1;
    }

    private long phoneTimeScores() {
        return (long) (Math.floor(getPhone_timeWithMinute() / 5) * 2);
    }

    // 将秒转换成小时，以供计算使用
    private float getPhoneTimeHour() {
        return phone_time / 3600;
    }

    public int getUnlockColorByType() {
        int base = 1;
        switch (type) {
            case DAY:
                base = 1;
                break;
            case WEEK:
                base = 7;
                break;
            case MONTH:
                base = 30;
            case YEAR:
                base = 365;
            default:
                break;
        }
        // 是否是重度用户
        if (unLockScores() > phoneTimeScores() * 10) {
            if (unlock_times > 49 * base & unlock_times < 125 * base) {
                return Colors[1];
            } else if (unlock_times > 124 * base && unlock_times < 200 * base) {
                return Colors[2];
            } else if (unlock_times > 199 * base) {
                return Colors[3];
            } else {
                return Colors[0];
            }
        }// 检查是否是均等
        else if (unLockScores() + phoneTimeScores() > 119 * base
                 && unLockScores() + phoneTimeScores() < 190 * base) {
            return Colors[1];
        } else if (unLockScores() + phoneTimeScores() > 189 * base
                   && unLockScores() + phoneTimeScores() < 270 * base) {
            return Colors[2];
        } else if (unLockScores() + phoneTimeScores() > 269 * base) {
            return Colors[3];
        }
        // 按照常规次数检查设置
        else if (getPhoneTimeHour() > 2 * base & getPhoneTimeHour() < 4 * base) {
            return Colors[1];
        } else if (getPhoneTimeHour() > 3 * base & getPhoneTimeHour() < 6 * base) {
            return Colors[2];
        } else if (getPhoneTimeHour() > 5 * base) {
            return Colors[3];
        }
        return Colors[0];
    }

    public int getPhoneTimeColor() {
        int base = 1;
        switch (type) {
            case DAY:
                base = 1;
                break;
            case WEEK:
                base = 7;
                break;
            case MONTH:
                base = 30;
            case YEAR:
                base = 365;
            default:
                break;
        }

        // 检查是否是重度用户
        if (phoneTimeScores() > unLockScores() * 10) {
            if (getPhoneTimeHour() > 2 * base & getPhoneTimeHour() < 4 * base) {
                return Colors[1];
            } else if (getPhoneTimeHour() > 3 * base & getPhoneTimeHour() < 6 * base) {
                return Colors[2];
            } else if (getPhoneTimeHour() > 5 * base) {
                return Colors[3];
            } else {
                return Colors[0];
            }
        }
        // 检查是否是均等
        else if (unLockScores() + phoneTimeScores() > 119 * base
                 && unLockScores() + phoneTimeScores() < 190 * base) {
            return Colors[1];
        } else if (unLockScores() + phoneTimeScores() > 189 * base
                   && unLockScores() + phoneTimeScores() < 270 * base) {
            return Colors[2];
        } else if (unLockScores() + phoneTimeScores() > 269 * base) {
            return Colors[3];
        }
        // 常规检查时间
        else if (getPhoneTimeHour() > 2 * base & getPhoneTimeHour() < 4 * base) {
            return Colors[1];
        } else if (getPhoneTimeHour() > 3 * base & getPhoneTimeHour() < 6 * base) {
            return Colors[2];
        } else if (getPhoneTimeHour() > 5 * base) {
            return Colors[3];
        } else {
            return Colors[0];
        }

    }
}
