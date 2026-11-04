"""Set screenshot fixtures on the debug emulator without altering app code.

Run with the emulator stopped at the app level (am force-stop). The generated
Preferences protobuf uses the documented DataStore wire schema; only emulator
game data is changed. Requires adb and an installed debuggable application.
"""
import argparse
import subprocess
import time

ADB='/opt/homebrew/share/android-commandlinetools/platform-tools/adb'
PKG='com.rares.virtualpetpompi'

def varint(n):
    out=bytearray()
    while n>127:
        out.append((n&127)|128); n>>=7
    out.append(n)
    return bytes(out)

def field(number,value):
    if isinstance(value,int):
        return varint(number<<3)+varint(value)
    return varint(number<<3|2)+varint(len(value))+value

def entry(key,value,kind):
    if kind=='string': encoded=field(5,value.encode())
    elif kind=='set': encoded=field(6,b''.join(field(1,v.encode()) for v in value))
    elif kind=='int': encoded=field(3,value)
    elif kind=='long': encoded=field(4,value)
    else: raise ValueError(kind)
    return field(1,field(1,key.encode())+field(2,encoded))

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--hunger',type=int,default=100)
    parser.add_argument('--room',default='bg0')
    args=parser.parse_args()
    rooms=['bg0','bg1','bg2','bg3','sunset','night','ocean','blossom']
    now=int(subprocess.check_output([ADB,'shell','date','+%s'],text=True).strip())*1000
    values=[('difficulty','NORMAL','string'),('hunger',args.hunger,'int'),('lives',3,'int'),('hunger_anchor',now,'long'),('steps_earned',100000,'long'),('spent_coins',0,'int'),('owned_rooms',rooms,'set'),('selected_room',args.room,'string'),('daily_goal',10000,'int')]
    data=b''.join(entry(*v) for v in values)
    subprocess.run([ADB,'shell','am','force-stop',PKG],check=True)
    subprocess.run([ADB,'shell','run-as',PKG,'mkdir','-p','files/datastore'],check=True)
    subprocess.run([ADB,'shell',f"run-as {PKG} sh -c 'cat > files/datastore/game.preferences_pb'"],input=data,check=True)
    print(f'Fixture: hunger={args.hunger}, room={args.room}, 1000 walking coins; emulator only')

if __name__=='__main__': main()
