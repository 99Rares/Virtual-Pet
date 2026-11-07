"""Emulator-only screenshot fixtures for the personal-pet skin review.

Never addresses physical devices. Existing preferences are backed up separately;
use --restore after review to restore that exact saved preference file.
"""
import argparse
from pathlib import Path
import subprocess
from emulator_state import entry

ADB='/opt/homebrew/share/android-commandlinetools/platform-tools/adb'
SERIAL='emulator-5554'
PKG='com.rares.virtualpetpompi'

def adb(*args,**kwargs):
    return subprocess.run([ADB,'-s',SERIAL,*args],check=True,**kwargs)

def main():
    p=argparse.ArgumentParser()
    p.add_argument('--skin',choices=['classic','pupsy','kira'],default='classic')
    p.add_argument('--owned',default='classic')
    p.add_argument('--room',default='bg0')
    p.add_argument('--hunger',type=int,default=100)
    p.add_argument('--restore',action='store_true')
    args=p.parse_args()
    qemu=subprocess.check_output([ADB,'-s',SERIAL,'shell','getprop','ro.kernel.qemu'],text=True).strip()
    if qemu!='1':raise SystemExit('Refusing to change preferences outside the emulator')
    adb('shell','am','force-stop',PKG)
    if args.restore:
        folder=Path(Path('/tmp/pompi-personal-pets-backup-location.txt').read_text().strip())
        data=(folder/'game.preferences_pb').read_bytes()
    else:
        if not 0<=args.hunger<=100:raise SystemExit('Hunger must be 0..100')
        now=int(subprocess.check_output([ADB,'-s',SERIAL,'shell','date','+%s'],text=True).strip())*1000
        owned=args.owned.split(',')
        if args.skin not in owned:raise SystemExit('Selected skin must be owned in the fixture')
        values=[('difficulty','NORMAL','string'),('hunger',args.hunger,'int'),('lives',3,'int'),('hunger_anchor',now,'long'),('steps_earned',100000,'long'),('spent_coins',0,'int'),('owned_rooms',['bg0','bg1','bg2','bg3','sunset','night','ocean','blossom'],'set'),('selected_room',args.room,'string'),('owned_skins',owned,'set'),('selected_skin',args.skin,'string'),('daily_goal',10000,'int')]
        data=b''.join(entry(*v) for v in values)
    adb('shell','run-as',PKG,'mkdir','-p','files/datastore')
    adb('shell',f"run-as {PKG} sh -c 'cat > files/datastore/game.preferences_pb'",input=data)
    print('Original emulator preferences restored' if args.restore else f'Emulator fixture: {args.skin}, hunger {args.hunger}, room {args.room}')

if __name__=='__main__':main()
