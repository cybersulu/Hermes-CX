import math
import subprocess
import os

# Generate high resolution 512x512 logo: img_hermes_logo.png
# Gradient background from Northwest #1E40AF (Royal Blue) to Southeast #10B981 (Emerald Green)
# Center: (256, 256)
# Globe Outer radius = 100px (diameter 200px)
# Circular conversion arrows ring radius = 158px (diameter 316px)

draw_cmds = []

# 1. Subtle decorative background glow / orbit ring
draw_cmds.append('stroke "rgba(255,255,255,0.20)" stroke-width 4 fill none circle 256,256 256,60')

# 2. Circular conversion arrows
def arc_path(cx, cy, r, start_deg, end_deg, num_pts=36):
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

# Arrow 1: Upper arc (clockwise, from 205 deg to 342 deg, radius 162)
d1 = arc_path(256, 256, 162, 205, 342, 36)
draw_cmds.append(f'stroke "#FFFFFF" stroke-width 16 stroke-linecap round fill none path "{d1}"')

# Arrowhead 1: pointing down-right at tangent
# At 342 deg, x ~ 410, y ~ 206
# Tangent direction ~ (0.31, 0.95)
# Tip at (418, 230), Wings at (390, 195) and (440, 192), notch at (413, 202)
draw_cmds.append('stroke none fill "#FFFFFF" polygon 418,230 390,195 413,202 440,192')

# Arrow 2: Lower arc (counter-clockwise / continuing, from 25 deg to 162 deg, radius 162)
d2 = arc_path(256, 256, 162, 25, 162, 36)
draw_cmds.append(f'stroke "#FFFFFF" stroke-width 16 stroke-linecap round fill none path "{d2}"')

# Arrowhead 2: pointing up-left at tangent
# At 162 deg, x ~ 102, y ~ 306
# Tip at (94, 282), Wings at (122, 317) and (72, 320), notch at (99, 310)
draw_cmds.append('stroke none fill "#FFFFFF" polygon 94,282 122,317 99,310 72,320')

# 3. Globe Motif in white outline:
# Globe Outer circle: radius 105 px (bounds 151 to 361)
draw_cmds.append('stroke "#FFFFFF" stroke-width 13 fill none circle 256,256 256,151')

# Globe Equator (horizontal line across globe)
draw_cmds.append('stroke "#FFFFFF" stroke-width 10 fill none line 151,256 361,256')

# Globe Prime Meridian (vertical line through poles)
draw_cmds.append('stroke "#FFFFFF" stroke-width 10 fill none line 256,151 256,361')

# Globe Parallels (upper & lower curved latitude lines)
# Upper:
draw_cmds.append('stroke "#FFFFFF" stroke-width 8.5 fill none path "M 170,205 Q 256,224 342,205"')
# Lower:
draw_cmds.append('stroke "#FFFFFF" stroke-width 8.5 fill none path "M 170,307 Q 256,288 342,307"')

# Globe Meridians (left & right curved longitude lines)
# Left:
draw_cmds.append('stroke "#FFFFFF" stroke-width 8.5 fill none path "M 256,151 C 192,151 192,361 256,361"')
# Right:
draw_cmds.append('stroke "#FFFFFF" stroke-width 8.5 fill none path "M 256,151 C 320,151 320,361 256,361"')

with open('tmp/draw_globe_logo.txt', 'w') as f:
    f.write('\n'.join(draw_cmds))

print("Created tmp/draw_globe_logo.txt")
