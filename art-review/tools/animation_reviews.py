"""Side-by-side skin animation previews using the unchanged PetSprite timings."""
from PIL import Image,ImageDraw,ImageFont
from review_art import RES,REVIEW,FONT
SKINS=['classic','red_panda','polar_bear','astronaut','cozy']
CLIPS={'idle':(['idle1','idle2','idle3'],220),'hungry':(['hungry1','hungry2'],250),'dance':(['dance1','dance2','dance3','dance2','dance3','dance2','dance1','dance2','dance3','dance2','dance1'],130),'feed':(['feed1','feed2']*3,200),'wake':(['a'+str(i) for i in range(1,11)],110)}
out=REVIEW/'animations'; out.mkdir(exist_ok=True); font=ImageFont.truetype(FONT,15)
for clip,(sequence,ms) in CLIPS.items():
 frames=[]
 for frame in sequence:
  canvas=Image.new('RGBA',(1100,264),'#D8E0E7'); d=ImageDraw.Draw(canvas)
  for col,skin in enumerate(SKINS):
   name=frame if skin=='classic' else skin+'_'+(frame.replace('a','wake',1) if frame.startswith('a') else frame)
   im=Image.open(RES/(name+'.webp')).convert('RGBA').resize((210,228),Image.Resampling.LANCZOS); canvas.alpha_composite(im,(col*220+5,2)); d.text((col*220+8,242),skin,fill='#202830',font=font)
  frames.append(canvas)
 frames[0].save(out/(clip+'.webp'),'WEBP',save_all=True,append_images=frames[1:],duration=ms,loop=0,quality=86,method=6)
print('Five side-by-side animation previews saved with original playback order/timing')
