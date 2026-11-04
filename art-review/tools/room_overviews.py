"""Compact review of every pose on every room; room order matches the catalog."""
from PIL import Image,ImageDraw,ImageFont
from review_art import REVIEW,FONT
rooms=['bg0','bg1','bg2','bg3','sunset','night','ocean','blossom']
for skin in ['classic','red_panda','polar_bear','astronaut','cozy']:
    out=Image.new('RGB',(1600,1180),'#dce2e8')
    draw=ImageDraw.Draw(out)
    for i,room in enumerate(rooms):
        tile=Image.open(REVIEW/'room-checks'/f'{skin}-{room}.webp').resize((400,568),Image.Resampling.LANCZOS)
        out.paste(tile,((i%4)*400,(i//4)*590))
        draw.text(((i%4)*400+6,(i//4)*590+570),room,fill='#202830',font=ImageFont.truetype(FONT,14))
    out.save(REVIEW/f'{skin}-rooms-overview.png')
