<?php

abstract class Config { 

    const PinRebootButton = 38; // Physical pin number https://www.elektronik-kompendium.de/sites/raspberry-pi/1907101.htm
    const ShortPressMs = 70;
    const LongPressMs = 2000;
    const RestartDisplayCommand = "/home/display/LEDDisplay/run_silent";

}

?>
