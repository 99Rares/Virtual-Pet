"""Compose the approved panda into Android's adaptive-icon safe circle."""
from pathlib import Path
import json
import math
import subprocess
import xml.etree.ElementTree as ET
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).resolve().parents[2]
RES=ROOT/'app/src/main/res/drawable'
OUT=ROOT/'art-review'
im=Image.open(ROOT/'art-review/registered/classic/idle1.png').convert('RGBA')
box=im.getchannel('A').point(lambda v:255 if v>=128 else 0).getbbox()
im=im.crop((box[0]-3,box[1]-3,box[2]+3,box[3]+3))
im.thumbnail((630,630),Image.Resampling.LANCZOS)
foreground=Image.new('RGBA',(1080,1080))
foreground.alpha_composite(im,((1080-im.width)//2,(1080-im.height)//2))
foreground.save(RES/'ic_launcher_foreground.webp','WEBP',quality=92,method=6,exact=True)
foreground.save(OUT/'registered/ic_launcher_foreground.png')
mask=foreground.getchannel('A')
radius=max(math.hypot(x-540,y-540) for y in range(1080) for x in range(1080) if mask.getpixel((x,y))>=128)
assert radius<=330, radius
(OUT/'launcher-safe-zone.json').write_text(json.dumps({'canvas_dp':108,'safe_circle_diameter_dp':66,'solid_foreground_max_radius_dp':round(radius/10,2)},indent=2)+'\n')
background=Image.new('RGBA',(1080,1080),'#365B91')
background.alpha_composite(foreground)
variants=[]
for shape in ['circle','rounded-square']:
    # Adaptive launchers mask the central 72dp viewport of the 108dp layer.
    visible=background.crop((180,180,900,900)).resize((240,240),Image.Resampling.LANCZOS)
    m=Image.new('L',(240,240)); d=ImageDraw.Draw(m)
    if shape=='circle': d.ellipse((0,0,239,239),fill=255)
    else: d.rounded_rectangle((0,0,239,239),radius=56,fill=255)
    visible.putalpha(m); variants.append(visible)
# Render the actual Android monochrome paths for a matching review preview.
ns='{http://schemas.android.com/apk/res/android}'
root=ET.parse(RES/'ic_launcher_monochrome.xml').getroot()
paths=''.join('<path fill="%s" fill-rule="%s" d="%s"/>'%('#D8E8EE','evenodd' if e.get(ns+'fillType')=='evenOdd' else 'nonzero',e.get(ns+'pathData')) for e in root)
svg='<svg xmlns="http://www.w3.org/2000/svg" width="1080" height="1080" viewBox="0 0 108 108">'+paths+'</svg>'
(OUT/'launcher-monochrome.svg').write_text(svg)
subprocess.run(['magick','-background','none',str(OUT/'launcher-monochrome.svg'),str(OUT/'launcher-monochrome.png')],check=True)
mono=Image.new('RGBA',(1080,1080),'#304C56'); mono.alpha_composite(Image.open(OUT/'launcher-monochrome.png').convert('RGBA'))
mono=mono.crop((180,180,900,900)).resize((240,240),Image.Resampling.LANCZOS); mono.putalpha(m); variants.append(mono)
sheet=Image.new('RGB',(900,310),'#E2E6EA'); draw=ImageDraw.Draw(sheet); font=ImageFont.truetype('/System/Library/Fonts/Supplemental/Arial.ttf',17)
for i,(v,label) in enumerate(zip(variants,['Circle mask','Rounded-square mask','Themed monochrome'])):
    sheet.paste(v,(30+i*300,15),v); draw.text((30+i*300,270),label,fill='#202830',font=font)
sheet.save(OUT/'launcher-review.png')
