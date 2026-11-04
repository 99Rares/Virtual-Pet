"""Measure exports and make review sheets; does not alter source artwork."""
from pathlib import Path
import json
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / 'app/src/main/res/drawable'
REVIEW = ROOT / 'art-review'
FONT = '/System/Library/Fonts/Supplemental/Arial.ttf'
FRAMES = [f'idle{i}' for i in range(1, 4)] + [f'hungry{i}' for i in range(1, 3)] + [f'dance{i}' for i in range(1, 4)] + [f'feed{i}' for i in range(1, 3)] + [f'a{i}' for i in range(1, 11)]
FOODS = 'rice cirese clatitecucafea englishbreakfast fruits1 cafea carbonara salad soup steak1 steak2 cartofipai burger cola hotdog nuggets onionrings gogosi pizza'.split()

def bounds(image):
    return image.getchannel('A').point(lambda v: 255 if v >= 128 else 0).getbbox()

def sheet(names, output, cols=5, cell=(180, 220), background=(218, 225, 232, 255), guides=False):
    canvas = Image.new('RGBA', (cols * cell[0], ((len(names) + cols - 1)//cols)*cell[1]), background)
    draw = ImageDraw.Draw(canvas)
    font = ImageFont.truetype(FONT, 12)
    for i, name in enumerate(names):
        path = RES / (name + '.webp')
        im = Image.open(path).convert('RGBA')
        im.thumbnail((cell[0]-12, cell[1]-28), Image.Resampling.LANCZOS)
        x = (i % cols)*cell[0] + (cell[0]-im.width)//2
        y = (i // cols)*cell[1]
        canvas.alpha_composite(im, (x, y))
        if guides:
            base = y + round(590/650*im.height)
            draw.line((i % cols*cell[0], base, (i % cols+1)*cell[0], base), fill='#B87878', width=1)
            cx = (i % cols)*cell[0]+cell[0]//2
            draw.line((cx, y, cx, y+im.height), fill=(140, 155, 170, 100), width=1)
        draw.text(((i % cols)*cell[0]+8, y+cell[1]-22), name, font=font, fill='#202830')
    canvas.convert('RGB').save(REVIEW/output)

def main():
    if all((RES/(n+'.webp')).exists() for n in FRAMES):
        sheet(FRAMES, 'classic-contact-sheet.png', cols=10, guides=True)
        sheet(FRAMES, 'classic-frames-row.png', cols=20, cell=(120, 154), guides=True)
    if all((RES/(n+'.webp')).exists() for n in FOODS):
        sheet(FOODS, 'food-contact-sheet.png')
    for skin in ['red_panda', 'polar_bear', 'astronaut', 'cozy']:
        names = [skin+'_'+(n.replace('a', 'wake', 1) if n.startswith('a') else n) for n in FRAMES]
        if all((RES/(n+'.webp')).exists() for n in names):
            sheet(names, skin+'-contact-sheet.png', guides=True)
    report = []
    for path in sorted(RES.glob('*.webp')):
        im = Image.open(path).convert('RGBA')
        report.append(dict(file=path.name, size=list(im.size), bytes=path.stat().st_size, alpha_extrema=im.getchannel('A').getextrema(), solid_bounds=bounds(im)))
    (REVIEW/'export-inventory.json').write_text(json.dumps(report, indent=2)+'\n')

if __name__ == '__main__':
    main()
