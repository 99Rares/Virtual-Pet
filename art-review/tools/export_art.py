"""Deterministic sizing/registration of generated artwork, with PNG masters retained."""
from pathlib import Path
import json
import sys
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT/'app/src/main/res/drawable'
GEN = ROOT/'art-review/generated'
REGISTERED = ROOT/'art-review/registered'

def solid_bounds(im):
    return im.getchannel('A').point(lambda v: 255 if v >= 128 else 0).getbbox()

def save(im, name, master_dir, limit=100_000):
    master_dir.mkdir(parents=True, exist_ok=True)
    im.save(master_dir/(name+'.png'))
    dest = RES/(name+'.webp')
    for quality in [92, 90, 85, 80]:
        im.save(dest, 'WEBP', quality=quality, method=6, exact=True)
        if dest.stat().st_size < limit:
            break
    if dest.stat().st_size >= limit:
        raise RuntimeError(f'{name}: size budget exceeded')
    for ext in ['.png', '.jpg', '.jpeg']:
        old = RES/(name+ext)
        if old.exists():
            old.unlink()

def classic():
    measurements = []
    for src in sorted((GEN/'classic').glob('*.png')):
        name = src.stem
        im = Image.open(src).convert('RGBA').resize((600,650), Image.Resampling.LANCZOS)
        # Keep breathing/pose changes; only register each canvas to common ground and foot centre.
        box = solid_bounds(im)
        bottom = box[3]
        strip = im.getchannel('A').crop((0,bottom-24,600,bottom)).point(lambda v: 255 if v >= 128 else 0).getbbox()
        foot_centre = (strip[0]+strip[2])/2
        dx, dy = round(300-foot_centre), 590-bottom
        canvas = Image.new('RGBA',(600,650))
        canvas.alpha_composite(im,(dx,dy))
        save(canvas,name,REGISTERED/'classic')
        measurements.append(dict(frame=name,shift=[dx,dy],solid_bounds=solid_bounds(canvas)))
    # A perfectly identical final wake frame makes the return to idle seamless.
    if (REGISTERED/'classic/idle1.png').exists():
        idle = Image.open(REGISTERED/'classic/idle1.png').convert('RGBA')
        save(idle,'a10',REGISTERED/'classic')
        save(idle,'classic_preview',REGISTERED/'classic')
    (ROOT/'art-review/classic-registration.json').write_text(json.dumps(measurements,indent=2)+'\n')

def objects(group):
    for src in sorted((GEN/group).glob('*.png')):
        im=Image.open(src).convert('RGBA')
        box=solid_bounds(im)
        # Use alpha > 0 for the crop margin, preserve soft edge pixels around the solid object.
        crop=(max(0,box[0]-12),max(0,box[1]-12),min(im.width,box[2]+12),min(im.height,box[3]+12))
        im=im.crop(crop)
        im.thumbnail((480,480),Image.Resampling.LANCZOS)
        canvas=Image.new('RGBA',(600,600))
        canvas.alpha_composite(im,((600-im.width)//2,(600-im.height)//2))
        save(canvas,src.stem,REGISTERED/group,150_000)

def skins(skin):
    for src in sorted((GEN/'skins'/skin).glob('*.png')):
        name=src.stem
        frame=name[len(skin)+1:]
        reference=frame.replace('wake','a')
        classic=Image.open(REGISTERED/'classic'/(reference+'.png')).convert('RGBA')
        im=Image.open(src).convert('RGBA').resize((600,650),Image.Resampling.LANCZOS)
        box=solid_bounds(im)
        # Tail/suit can widen the silhouette; anchor only the lowest foot-contact strip.
        strip=im.getchannel('A').crop((0,box[3]-18,600,box[3])).point(lambda v: 255 if v >=128 else 0).getbbox()
        cx=(strip[0]+strip[2])/2
        canvas=Image.new('RGBA',(600,650))
        canvas.alpha_composite(im,(round(300-cx),590-box[3]))
        save(canvas,name,REGISTERED/'skins'/skin)
    idle=REGISTERED/'skins'/skin/(skin+'_idle1.png')
    if idle.exists():
        im=Image.open(idle).convert('RGBA')
        save(im,skin+'_preview',REGISTERED/'skins'/skin)
        save(im,skin+'_wake10',REGISTERED/'skins'/skin)

if __name__=='__main__':
    group=sys.argv[1]
    if group=='classic': classic()
    elif group in ['food','icons']: objects(group)
    else: skins(group)
