# Remote Shoot Notes #

The remote shoot works by hooking functions that write the JPEG to a file and instead dump it into memory (i think, based on looking at source) The first script sets up the hook and sends a timeout when reached will result in the hook removed. The next script takes the photo using the normal shoot() function and then pulls it over the USB.

* >> Script STATUS packet (0x08)
* << Reply 0x2001 (ok)
* >> Execute Script
* >> Script
```
function rs_init(opts)
	local rec,vid = get_mode()
	if not rec then
		return false,'not in rec mode'
	end
	if type(init_usb_capture) ~= 'function' then
		return false, 'usb capture not supported'
	end
	if bitand(get_usb_capture_support(),opts.fformat) ~= opts.fformat then
		return false, 'unsupported format'
	end
	if not init_usb_capture(opts.fformat,opts.lstart,opts.lcount) then
		return false, 'init failed'   
	end
	if opts.cap_timeout then
		set_usb_capture_timeout(opts.cap_timeout)
	end
	if opts.cont then
		if get_prop(require'propcase'.DRIVE_MODE) ~= 1 then
			return false, 'not in continuous mode'
		end
		if opts.cont <= 0 then
			return false, 'invalid shot count'
		end
	end
	return true
end

return rs_init({
 lcount=0,
 lstart=0,
 fformat=1,
})
```

* >> Check script status
* << Reply 0x2001 ARG0: 0x0002
* >> Read Script Message (0x0A)
* << Reply 0x9999 Arg0 0x0001
* >> check script status
* << Reply 0x2001 ARG0:0x00
* >> execute script
* >> Script
```
function rlib_shoot_init_exp (opts)
if opts.tv then
		set_tv96_direct(opts.tv)
	end
	if opts.sv then
		set_sv96(opts.sv)
	end
	if opts.isomode then
		set_iso_mode(opts.isomode)
	end
	if opts.av then
		set_av96_direct(opts.av)
	end
	if opts.nd then
		set_nd_filter(opts.nd)
	end
end

function rs_shoot_single()
	shoot()
end
function rs_shoot_cont(opts)
	local last = get_exp_count() + opts.cont
	press('shoot_half')
	repeat
		m=read_usb_msg(10)
	until get_shooting() or m == 'stop'
	if m == 'stop' then
		release('shoot_half')
		return
	end
	sleep(20)
	press('shoot_full')
	repeat
		m=read_usb_msg(10)
	until get_exp_count() >= last or m == 'stop'
	release('shoot_full')
end
function rs_shoot(opts)
	rlib_shoot_init_exp(opts)
	if opts.cont then
		rs_shoot_cont(opts)
	else
		rs_shoot_single()
	end
end

rs_shoot({
 lcount=0,
 lstart=0,
 fformat=1,
})

```
* << ok (0x2001) 0x0d 0x00
* >> script status
* << OK 0x01
* >> Remote Capture is Ready? (0x0d)
* << OK 0x00 0x00
* >> Script Status
* << 0K 0x01
* Repeat
* >> Remote capture is ready?
* << OK 0x01 0xb01b7
* >> Get JPEG
* << crap tons of jpeg


