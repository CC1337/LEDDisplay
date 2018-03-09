<?php

require_once "rebootd_config.php";
require_once "rebootd_gpio.php";

function msec()
{
    list($usec, $sec) = explode(' ',microtime());
    return intval(($usec+$sec)*1000.0);
}

function reboot_os() {
    shell_exec("reboot");
}

function restart_display() {
    shell_exec(Config::RestartDisplayCommand);
}


Gpio::setUp();

$buttonPressedTime = 0;

while(true) {

//echo (Gpio::isRebootButtonPressed() == true ? "1" : "0")."
//";
    if (Gpio::isRebootButtonPressed()) {
        if ($buttonPressedTime == 0) {
            $buttonPressedTime = msec();
        }
    } else if ($buttonPressedTime != 0) { 
        $buttonReleasedTime = msec();

        if ($buttonReleasedTime - $buttonPressedTime > Config::LongPressMs) {
            echo $buttonReleasedTime - $buttonPressedTime;
            $buttonPressedTime = 0;
            reboot_os();
            sleep(5);
        } else if ($buttonReleasedTime - $buttonPressedTime > Config::ShortPressMs) {
            echo $buttonReleasedTime - $buttonPressedTime;
            $buttonPressedTime = 0;
            restart_display();
            sleep(5);
        }
    }

    usleep(50000);
}

?>
