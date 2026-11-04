"""Review every frame over all eight rooms, without modifying Kotlin.
Bitmap rooms use shipping art. Painted scenes reproduce RoomBackground.kt's
palette/layout; seeded scatter is representative, not pixel-identical Kotlin RNG.
Actual app room screenshots provide the runtime check as well.
"""
from pathlib import Path
import math,random
from PIL import Image,ImageDraw,ImageFont
from review_art import FRAMES,RES,REVIEW,FONT
W,H=200,333

def rgb(c): return tuple(int(c[i:i+2],16) for i in (0,2,4))
def gradient(cs):
 cs=[rgb(c) for c in cs]; im=Image.new('RGBA',(W,H)); d=ImageDraw.Draw(im)
 for y in range(H):
  t=y/(H-1)*(len(cs)-1); k=min(int(t),len(cs)-2); f=t-k
  d.line((0,y,W,y),fill=tuple(round(a+(b-a)*f) for a,b in zip(cs[k],cs[k+1]))+(255,))
 return im

def hill(im,colour,base,amp,phase,waves=1):
 pts=[(0,H)]+[(x,base+amp*math.sin(x/W*2*math.pi*waves+phase)) for x in range(W+1)]+[(W,H)]
 layer=Image.new('RGBA',(W,H)); ImageDraw.Draw(layer).polygon(pts,fill=colour); im.alpha_composite(layer)

def scene(room):
 if room.startswith('bg'): return Image.open(RES/(room+'.webp')).convert('RGBA').resize((W,H),Image.Resampling.LANCZOS)
 if room=='sunset':
  im=gradient(['2B1B4D','B5416B','FF8A4C','FFC46B']); d=ImageDraw.Draw(im); x,y,r=W*.5,H*.62,W*.22
  d.ellipse((x-r,y-r,x+r,y+r),fill='#FFE9A8'); hill(im,'#4A2358',H*.80,H*.06,0); hill(im,'#2A1238',H*.88,H*.05,1.7)
 elif room=='night':
  im=gradient(['050A24','14214F','2A3F7A']); d=ImageDraw.Draw(im); rng=random.Random(0)
  for i in range(90):
   x,y=rng.random()*W,rng.random()*H*.85; r=.45+(i%4)*.3; d.ellipse((x-r,y-r,x+r,y+r),fill='#D2DCF0')
  x,y,r=W*.74,H*.17,W*.09; d.ellipse((x-r,y-r,x+r,y+r),fill='#FFF6D6'); hill(im,'#0B1235',H*.9,H*.04,.6)
 elif room=='ocean':
  im=gradient(['0A4D68','088395','7ED4D1'])
  for i in range(5): hill(im,(255,255,255,26),H*(.35+i*.13),H*.018,i*1.3,2)
 else:
  im=gradient(['FFD6E0','FFA5BE','E87AA0']); layer=Image.new('RGBA',(W,H)); d=ImageDraw.Draw(layer); rng=random.Random(0)
  for i in range(45):
   x,y=rng.random()*W,rng.random()*H; r=W*(.012+(i%5)*.008); d.ellipse((x-r,y-r,x+r,y+r),fill=(255,255,255,round((.22+(i%4)*.08)*255)))
  im.alpha_composite(layer)
 return im

def main():
 output=REVIEW/'room-checks'; output.mkdir(exist_ok=True); rooms=['bg0','bg1','bg2','bg3','sunset','night','ocean','blossom']; font=ImageFont.truetype(FONT,12)
 for skin in ['classic','red_panda','polar_bear','astronaut','cozy']:
  names=FRAMES if skin=='classic' else [skin+'_'+(n.replace('a','wake',1) if n.startswith('a') else n) for n in FRAMES]
  if not all((RES/(n+'.webp')).exists() for n in names): continue
  for room in rooms:
   bg=scene(room); sheet=Image.new('RGB',(5*W,4*(H+22)),'#DCE2E8'); d=ImageDraw.Draw(sheet)
   for i,n in enumerate(names):
    tile=bg.copy(); sprite=Image.open(RES/(n+'.webp')).convert('RGBA').resize((180,195),Image.Resampling.LANCZOS); tile.alpha_composite(sprite,(10,112)); sheet.paste(tile.convert('RGB'),((i%5)*W,(i//5)*(H+22))); d.text(((i%5)*W+6,(i//5)*(H+22)+H+3),n,fill='#202830',font=font)
   sheet.save(output/(skin+'-'+room+'.webp'),'WEBP',quality=88,method=6)
 all_rooms=Image.new('RGB',(4*W,2*(H+24)),'#DCE2E8'); d=ImageDraw.Draw(all_rooms)
 for i,room in enumerate(rooms):
  all_rooms.paste(scene(room).convert('RGB'),((i%4)*W,(i//4)*(H+24))); d.text(((i%4)*W+6,(i//4)*(H+24)+H+3),room,fill='#202830',font=font)
 all_rooms.save(REVIEW/'rooms-contact-sheet.png')
if __name__=='__main__': main()
