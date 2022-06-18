from http.server import BaseHTTPRequestHandler, HTTPServer
import subprocess
import urllib.request
import signal
import os


class handler(BaseHTTPRequestHandler):
    def do_POST(self):
        # stop nifi flow
        subprocess.call(['sh', './stop-nifi-flow.sh'])
        os.kill(os.getpid(), signal.SIGTERM)



with HTTPServer(('172.17.0.1', 5555), handler) as server:
    subprocess.call(['sh', './start-hdfs.sh'])

    # start nifi flow when the web UI is up and running
    message = 0
    while (message != 200):
        try:
            message = urllib.request.urlopen("http://localhost:8090/nifi").getcode()
            print("Nifi Web UI is up and running!")
            print(message)
        except:
            print("Nifi not running...")
        
    subprocess.call(['sh', './start-nifi-flow.sh'])
    server.serve_forever()