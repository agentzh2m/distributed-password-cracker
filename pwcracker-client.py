#! /usr/bin/env python

from __future__ import print_function
import requests
import sys
import time

def mk_url(host, port):
	return "http://" + host + ":" + port + "/"

def send_job(host, port, hash):
	r = requests.post(mk_url(host, port) + "send_job", json={"hash": hash})
	if r.status_code == 200:
		return r.json()
	else:
		return 'request fail' 

def get_status(host, port, hash=None):
	url = mk_url(host, port) + "status"
	if hash is not None:
		url += '?hash=' + hash
	r = requests.get(url)
	if r.status_code == 200:
		return r.json()
	else:
		return 'request fail'


def kill_server(host, port):
	r = requests.get(mk_url(host, port) + "kill")
	if r.status_code == 200:
		return r.json()
	else:
		return 'request fail'

if len(sys.argv) < 4:
	print("need to specify host and post i.e. ./client host port command")
	print("command can be the following send <hash>, status and kill")
else:
	hostname = sys.argv[1]
	port = sys.argv[2]
	cmd = sys.argv[3]
	if cmd == "send" and len(sys.argv) == 5:
		hash = sys.argv[4]
		print(send_job(hostname, port, hash))
	elif cmd == "status":
		if len(sys.argv) == 5:
			hash = sys.argv[4]
			print(get_status(hostname, port, hash=hash))
		else:
			print(get_status(hostname, port))
	elif cmd == "kill":
		print(kill_server(hostname, port))
	elif cmd == "poll" and len(sys.argv) == 5:
		hash = sys.argv[4]
		while(True):
			time.sleep(5)
			status = get_status(hostname, port, hash=hash)
			if len(status[hash]) > 0:
				print('the password is ' + status[hash])
				break


	else:
		print("no command matching the request")




