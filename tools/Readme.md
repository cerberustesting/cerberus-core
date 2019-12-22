# Problem glossary


## Connect mobile devices with a self-powered hub 

If you use a self-powered hub, you can have some deconnection problem.
Verify the power option of your usb. If option is standby if do nothing, you can have problem.

On linux, you can set it : 

`/your/path/to/maintainsPortUsbOk.sh`
```
#!/bin/sh
  
for dev in /sys/bus/usb/devices/*/power/control; do         echo $dev;         echo on > $dev; done

for dev in /sys/bus/usb/devices/*/*/power/control; do         echo $dev;         echo on > $dev; done
```

and add to your cron tab : 

`$ chmod +x /your/path/to/maintainsPortUsbOk.sh && crontab -e`
```
* * * * * /your/path/to/maintainsPortUsbOk.sh
```

