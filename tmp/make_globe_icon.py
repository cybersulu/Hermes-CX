import math
import subprocess
import os

# We will generate:
# 1) app/src/main/res/drawable/img_hermes_logo.png (512x512)
#    With vibrant diagonal blue-to-green gradient background (#1E40AF to #10B981)
#    White outline globe motif with latitude and longitude curves
#    Circular counter-rotating / circulating arrows representing conversion process
# 2) app/src/main/res/drawable/ic_launcher_background.xml (vector gradient #1E40AF to #10B981)
# 3) app/src/main/res/drawable/ic_launcher_foreground.xml (vector foreground globe + arrows in 108dp canvas, centered within 66dp)
# 4) app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.png and ic_launcher_round.png
# 5) app/src/main/res/drawable/splash_background.xml & colors.xml & themes.xml

# Let's write the draw command file for ImageMagick
# Canvas size: 512 x 512, Center: (256, 256)

draw_cmds = []

# Style parameters:
# Globe center (256, 256), Globe radius R = 110 px
# Arrows ring radius R_arr = 175 px

# 1. Globe Outer Circle (white outline, stroke width ~10px)
draw_cmds.append('stroke "#FFFFFF" stroke-width 10 fill none circle 256,256 256,146')

# 2. Globe Equator (horizontal line across globe from (146, 256) to (366, 256))
draw_cmds.append('stroke "#FFFFFF" stroke-width 8 fill none line 146,256 366,256')

# 3. Globe Latitude Parallels (upper & lower arcs)
# Upper parallel: from (178, 190) across (256, 205) to (334, 190)
# Lower parallel: from (178, 322) across (256, 307) to (334, 322)
# Using bezier or arc / path:
draw_cmds.append('stroke "#FFFFFF" stroke-width 7 fill none path "M 166,200 Q 256,220 346,200"')
draw_cmds.append('stroke "#FFFFFF" stroke-width 7 fill none path "M 166,312 Q 256,292 346,312"')

# 4. Globe Prime Meridian (vertical ellipse / arc):
# Vertical line through poles (256, 146) to (256, 366)
draw_cmds.append('stroke "#FFFFFF" stroke-width 8 fill none line 256,146 256,366')

# Left meridian ellipse arc:
# M 256,146 C 185,146 185,366 256,366
draw_cmds.append('stroke "#FFFFFF" stroke-width 7 fill none path "M 256,146 C 190,146 190,366 256,366"')
# Right meridian ellipse arc:
# M 256,146 C 322,146 322,366 256,366
draw_cmds.append('stroke "#FFFFFF" stroke-width 7 fill none path "M 256,146 C 322,146 322,366 256,366"')

# 5. Circular Conversion Arrows around the globe:
# A circular arc on top-right moving clockwise: from angle ~ 160 deg to ~ 340 deg (or 200 deg to 380 deg)
# Let's define two large curved conversion arrows:
# Radius R_arr = 168 px, stroke-width = 12 px, stroke-linecap round
# Arrow 1: Upper arc sweeping clockwise from (-145 deg) to (25 deg):
# Start at ~ (-135 deg): x = 256 + 168*cos(-135) = 256 - 118.8 = 137.2, y = 256 + 168*sin(-135) = 256 - 118.8 = 137.2
# Let's generate points along circular arc from 195 deg to 345 deg (or -165 deg to -15 deg):
def arc_path(cx, cy, r, start_deg, end_deg, num_pts=30):
    pts = []
    for i in range(num_pts + 1):
        deg = start_deg + (end_deg - start_deg) * (i / num_pts)
        rad = math.radians(deg)
        x = cx + r * math.cos(rad)
        y = cy + r * math.sin(rad)
        pts.append((x, y))
    d = f"M {pts[0][0]:.1f},{pts[0][1]:.1f}"
    for pt in pts[1:]:
        d += f" L {pt[0]:.1f},{pt[1]:.1f}"
    return d

# Arrow 1: From 205 deg to 340 deg (upper arc sweeping right)
d1 = arc_path(256, 256, 172, 205, 342, 40)
draw_cmds.append(f'stroke "#FFFFFF" stroke-width 13 stroke-linecap round fill none path "{d1}"')

# Arrowhead 1 at ~ 345 deg: end point is (256 + 172*cos(342), 256 + 172*sin(342)) = (419.6, 202.9)
# Arrow points down-right tangential to 342 deg -> tangent angle is 342 + 90 = 432 = 72 deg (pointing downwards/right)
# Let's make a crisp arrow head polygon pointing along tangent:
# Tip at (426, 222)
# Wing 1 at (398, 178)
# Wing 2 at (448, 184)
# Central indent at (422, 192)
draw_cmds.append('stroke none fill "#FFFFFF" polygon 429,228 395,188 420,195 448,185')

# Arrow 2: Lower arc sweeping left from 25 deg to 162 deg
d2 = arc_path(256, 256, 172, 25, 162, 40)
draw_cmds.append(f'stroke "#FFFFFF" stroke-width 13 stroke-linecap round fill none path "{d2}"')

# Arrowhead 2 at ~ 165 deg: tip pointing up-left (tangent at 162 + 90 = 252 deg)
# Tip at (83, 284)
# Wing 1 at (114, 324)
# Wing 2 at (64, 327)
# Indent at (92, 317)
draw_cmds.append('stroke none fill "#FFFFFF" polygon 83,284 117,324 92,317 64,327')

with open('/tmp/draw_globe_icon.txt', 'w') as f:
    f.write('\n'.join(draw_cmds))

print("Created /tmp/draw_globe_icon.txt with", len(draw_cmds), "instructions")
