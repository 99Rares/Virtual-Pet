"""Export and review Pupsy/Kira without changing the app's other artwork.

Run with Python + Pillow from any directory. --export registers raw generated
masters using the established export_art.py workflow; all other work is read-only
compositing and validation. Photo-based artistic changes use built-in imagegen.
"""
from pathlib import Path
import argparse
import hashlib
import json
from PIL import Image, ImageDraw, ImageFont
from export_art import skins as export_skin, solid_bounds
from review_art import ROOT, RES, FONT
from room_reviews import scene

SKINS = ['pupsy', 'kira']
FRAMES = [f'{kind}{i}' for kind, count in [('idle',3),('hungry',2),('dance',3),('feed',2),('wake',10)] for i in range(1,count+1)]
ROOMS = ['bg0','bg1','bg2','bg3','sunset','night','ocean','blossom']
CLIPS = {
    'idle': (['idle1','idle2','idle3'],220),
    'hungry': (['hungry1','hungry2'],250),
    'dance': (['dance1','dance2','dance3','dance2','dance3','dance2','dance1','dance2','dance3','dance2','dance1'],130),
    'feed': (['feed1','feed2']*3,200),
    'wake': ([f'wake{i}' for i in range(1,11)],110),
}
OUT = ROOT/'art-review/personal-pets'
FONT_SMALL = ImageFont.truetype(FONT,13)

def sprite(skin, frame):
    return Image.open(RES/f'{skin}_{frame}.webp').convert('RGBA')

def contact(skin):
    image = Image.new('RGB',(1000,1000),'#E4EAF0')
    d = ImageDraw.Draw(image)
    for i, frame in enumerate(FRAMES):
        tile = Image.new('RGBA',(200,250),'#E4EAF0')
        art = sprite(skin,frame).resize((200,217),Image.Resampling.LANCZOS)
        tile.alpha_composite(art,(0,0))
        t = ImageDraw.Draw(tile)
        t.line((0,197,200,197),fill='#B87878')
        t.line((100,0,100,217),fill='#AAB6C0')
        t.text((8,227),frame,font=FONT_SMALL,fill='#202830')
        image.paste(tile.convert('RGB'),((i%5)*200,(i//5)*250))
    image.save(OUT/f'{skin}-contact-sheet.png')


def room_checks(skin):
    overview = Image.new('RGB',(1600,1180),'#E4EAF0')
    od = ImageDraw.Draw(overview)
    for ri, room in enumerate(ROOMS):
        full = Image.new('RGB',(1000,1420),'#E4EAF0')
        d = ImageDraw.Draw(full)
        for i,frame in enumerate(FRAMES):
            tile = scene(room)
            tile.alpha_composite(sprite(skin,frame).resize((180,195),Image.Resampling.LANCZOS),(10,112))
            x,y = (i%5)*200,(i//5)*355
            full.paste(tile.convert('RGB'),(x,y))
            d.text((x+5,y+335),frame,font=FONT_SMALL,fill='#202830')
        full.save(OUT/f'{skin}-{room}.webp','WEBP',quality=88,method=6)
        overview.paste(full.resize((400,568),Image.Resampling.LANCZOS),((ri%4)*400,(ri//4)*590))
        od.text(((ri%4)*400+5,(ri//4)*590+570),room,font=FONT_SMALL,fill='#202830')
    overview.save(OUT/f'{skin}-rooms-overview.png')


def animations():
    for clip,(sequence,ms) in CLIPS.items():
        frames=[]
        for frame in sequence:
            canvas=Image.new('RGBA',(840,330),'#E4EAF0')
            d=ImageDraw.Draw(canvas)
            for i,skin in enumerate(SKINS):
                for row,bg in enumerate(['#141820','#E4EAF0']):
                    tile=Image.new('RGBA',(210,290),bg)
                    tile.alpha_composite(sprite(skin,frame).resize((240,260),Image.Resampling.LANCZOS),(-15,0))
                    canvas.alpha_composite(tile,(i*420+row*210,0))
                    d.text((i*420+row*210+8,300),skin+' / '+frame,font=FONT_SMALL,fill='#202830')
            frames.append(canvas)
        frames[0].save(OUT/f'{clip}.webp','WEBP',save_all=True,append_images=frames[1:],duration=ms,loop=0,quality=86,method=6)


def verify():
    rows=[];failures=[]
    for skin in SKINS:
        for frame in FRAMES+['preview']:
            path=RES/f'{skin}_{frame}.webp'
            if not path.exists():failures.append('Missing '+path.name);continue
            art=Image.open(path).convert('RGBA');box=solid_bounds(art)
            if art.size!=(600,650):failures.append(path.stem+': canvas '+str(art.size))
            if art.getchannel('A').getextrema()!=(0,255):failures.append(path.stem+': alpha')
            if box[3]!=590:failures.append(path.stem+': baseline '+str(box[3]))
            strip=art.getchannel('A').crop((0,572,600,590)).point(lambda a:255 if a>=128 else 0).getbbox()
            if strip is None or abs((strip[0]+strip[2])/2-300)>1:failures.append(path.stem+': foot center')
            if min(box[:2])<8 or box[2]>592:failures.append(path.stem+': clipped margin')
            if path.stat().st_size>=100000:failures.append(path.stem+': size budget')
            rows.append(dict(file=path.name,bytes=path.stat().st_size,solid_bounds=box))
        if not failures:
            idle=sprite(skin,'idle1')
            for f in ['wake10','preview']:
                if idle.tobytes()!=sprite(skin,f).tobytes():failures.append(skin+': '+f+' differs from idle1')
    meta_path=ROOT/'art-review/generated/skins/kira/kira_idle1.json'
    expected=json.loads(meta_path.read_text())['selected_sha256']
    if hashlib.sha256(meta_path.with_suffix('.png').read_bytes()).hexdigest()!=expected:
        failures.append('Selected Kira master changed')
    report=dict(images=len(rows),bytes=sum(r['bytes'] for r in rows),failures=failures,assets=rows)
    (OUT/'asset-checks.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps({k:v for k,v in report.items() if k!='assets'},indent=2))
    if failures:raise SystemExit(1)


def main():
    parser=argparse.ArgumentParser();parser.add_argument('--export',action='store_true');args=parser.parse_args()
    OUT.mkdir(exist_ok=True)
    if args.export:
        for skin in SKINS:export_skin(skin)
    verify()
    for skin in SKINS:
        contact(skin);room_checks(skin)
    animations()

if __name__=='__main__':main()
