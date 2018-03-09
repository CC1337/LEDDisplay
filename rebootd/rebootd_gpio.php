<?php

// http://wiringpi.com/the-gpio-utility/

class Gpio {

    public static function read($pin) {
        return intval(shell_exec('gpio -1 read '.$pin)) == 1;
    }

    public static function mode($pin, $mode) {
        shell_exec('gpio -1 mode '.$pin.' '.$mode);
    }

    public static function setUp() {
        self::mode(Config::PinRebootButton, 'in');
        self::mode(Config::PinRebootButton, 'down');
    }

    public static function isRebootButtonPressed() {
        return self::read(Config::PinRebootButton);
    }
}

?>