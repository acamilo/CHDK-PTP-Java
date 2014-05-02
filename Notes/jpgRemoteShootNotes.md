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


## set_usb_capture_timeout ##
What do you do? you're defaulting to something.
https://github.com/c10ud/CHDK/blob/master/modules/luascript.c line 2504 luaCB_set_usb_capture_timeout

timeout. miliseconds.
HOOK_WAIT_MAX_DEFAULT https://github.com/c10ud/CHDK/blob/master/core/remotecap.c 3000ms

## init_usb_capture ##
from https://github.com/c10ud/CHDK/blob/master/modules/luascript.c 2466
```
status=init_usb_capture(bitmask[,startline, numlines])
bitmask = 0 clear usb capture mode

lines only applies to raw
startline defaults to 0
numlines defaults to full buffer
```
Called (1,0,0)
c function remotecap_set_target(what,startline,numlines) luascript.c 2473 uaCB_init_usb_capture
dumped into remote_file_target

value | format
------|-------
1|jpeg
2|raw
4|DNG Header

## what gets sent back ? 33
its a jpeg according to code.
last 2 bytes of last data packet is FF d9. end of jpeg.
first bytes of first packet are not FFD8. IS there extra stuff before the jpeg starts? is it at an offset?
>  //client needs to seek to this file position before writing the chunk (-1 = ignore)

oh.
Camera sends a chunk+seek value when you call 0x000e
beginning of file matches one of the blocks. packet 646 in pcap.

Assuming no seek == stick it right after the previous one..
