"""Check the requested art contract and emit a factual verification report."""
import json,re,subprocess
from pathlib import Path
from PIL import Image
from review_art import ROOT,RES,REVIEW,FRAMES,FOODS,bounds

skins=['red_panda','polar_bear','astronaut','cozy']
expected=FRAMES+['classic_preview']+FOODS+['coinimage','heart','skull']+['bg0','bg1','bg2','bg3','ic_launcher_foreground']
for skin in skins:
 expected += [skin+'_'+(n.replace('a','wake',1) if n.startswith('a') else n) for n in FRAMES]+[skin+'_preview']
assert len(expected)==132
failures=[]; rows=[]; skin_total=0
for name in expected:
 p=RES/(name+'.webp')
 if not p.exists(): failures.append('Missing '+name); continue
 im=Image.open(p).convert('RGBA'); alpha=im.getchannel('A'); b=bounds(im); size=p.stat().st_size
 pet=name in FRAMES+['classic_preview'] or any(name.startswith(s+'_') for s in skins)
 room=name in ['bg0','bg1','bg2','bg3']; launcher=name=='ic_launcher_foreground'
 target=(600,650) if pet else (1500,2500) if room else (1080,1080) if launcher else (600,600)
 if im.size!=target: failures.append(name+': wrong canvas '+str(im.size))
 if room:
  if alpha.getextrema()!=(255,255): failures.append(name+': room must be opaque')
 elif alpha.getextrema()!=(0,255): failures.append(name+': missing real transparency/opaque subject')
 limit=100000 if any(name.startswith(s+'_') for s in skins) else 150000
 if size>=limit: failures.append(name+': exceeds size budget')
 if pet and b[3]!=590: failures.append(name+': ground baseline is '+str(b[3]))
 if pet and (b[0]<8 or b[1]<8 or b[2]>592): failures.append(name+': silhouette too close to canvas edge')
 if any(name.startswith(s+'_') for s in skins): skin_total+=size
 rows.append(dict(name=name,canvas=im.size,bytes=size,solid_bounds=b,alpha=alpha.getextrema()))
if skin_total>6000000: failures.append('Paid skins exceed 6 MB')
files=list(RES.iterdir()); stems=[p.stem for p in files if p.is_file()]
if len(stems)!=len(set(stems)): failures.append('Duplicate drawable resource names')
if any(p.suffix.lower() in ['.jpg','.jpeg'] for p in files): failures.append('JPEG still present')
if any(not re.fullmatch('[a-z][a-z0-9_]*',n) for n in stems): failures.append('Invalid Android resource name')
changed=subprocess.check_output(['git','diff','--name-only','main','--','*.kt'],cwd=ROOT,text=True).splitlines()
allowed=['app/src/main/java/com/rares/virtualpetpompi/ui/components/Common.kt']
if changed!=allowed: failures.append('Unexpected Kotlin changes: '+str(changed))
report=dict(expected_rasters=len(expected),checked_rasters=len(rows),paid_skin_images=84,paid_skin_bytes=skin_total,kotlin_changes=changed,failures=failures,assets=rows)
(REVIEW/'verification/asset-checks.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps({k:v for k,v in report.items() if k!='assets'},indent=2))
raise SystemExit(bool(failures))
