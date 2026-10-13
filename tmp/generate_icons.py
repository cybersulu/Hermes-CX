import subprocess
import os

# Create drawing commands for ImageMagick at 512x512 resolution
# Scale factor from 108dp viewport to 512px: 512 / 108 = 4.74074

def scale(val):
    return round(val * (512.0 / 108.0), 2)

draw_cmds = []

# Background dark gradient or circle
# Ambient subtle outer ring
cx, cy = scale(54), scale(54)
r_ring = scale(41)
draw_cmds.append(f'stroke "#1E293B" stroke-width {scale(1.5)} fill none circle {cx},{cy} {cx},{cy - r_ring}')
r_ring_inner = scale(38.5)
draw_cmds.append(f'stroke "#172554" stroke-width {scale(1.0)} fill none circle {cx},{cy} {cx},{cy - r_ring_inner}')

# Path coordinates scaled to 512x512
# Top arrow (Emerald Green #10B981)
# Original 108 coords:
# M54,20.5 C72.5,20.5 87.5,35.5 87.5,54 L93.5,54 L83.5,67 L73.5,54 L79.5,54 C79.5,40.0 68.0,28.5 54,28.5 C47.5,28.5 41.5,31.2 37.3,35.7 L30.8,29.2 C36.8,23.8 45.0,20.5 54,20.5 Z
p_top = (
    f"M {scale(54)},{scale(20.5)} "
    f"C {scale(72.5)},{scale(20.5)} {scale(87.5)},{scale(35.5)} {scale(87.5)},{scale(54)} "
    f"L {scale(94.5)},{scale(54)} L {scale(83.5)},{scale(67)} L {scale(72.5)},{scale(54)} L {scale(79.5)},{scale(54)} "
    f"C {scale(79.5)},{scale(40.0)} {scale(68.0)},{scale(28.5)} {scale(54)},{scale(28.5)} "
    f"C {scale(47.5)},{scale(28.5)} {scale(41.5)},{scale(31.2)} {scale(37.3)},{scale(35.7)} "
    f"L {scale(30.8)},{scale(29.2)} "
    f"C {scale(36.8)},{scale(23.8)} {scale(45.0)},{scale(20.5)} {scale(54)},{scale(20.5)} Z"
)
draw_cmds.append(f'stroke none fill "#10B981" path "{p_top}"')

# Bottom arrow (Cyan #06B6D4)
# Original 108 coords:
# M54,87.5 C35.5,87.5 20.5,72.5 20.5,54 L14.5,54 L24.5,41 L34.5,54 L28.5,54 C28.5,68.0 40.0,79.5 54,79.5 C60.5,79.5 66.5,76.8 70.7,72.3 L77.2,78.8 C71.2,84.2 63.0,87.5 54,87.5 Z
p_bottom = (
    f"M {scale(54)},{scale(87.5)} "
    f"C {scale(35.5)},{scale(87.5)} {scale(20.5)},{scale(72.5)} {scale(20.5)},{scale(54)} "
    f"L {scale(13.5)},{scale(54)} L {scale(24.5)},{scale(41)} L {scale(35.5)},{scale(54)} L {scale(28.5)},{scale(54)} "
    f"C {scale(28.5)},{scale(68.0)} {scale(40.0)},{scale(79.5)} {scale(54)},{scale(79.5)} "
    f"C {scale(60.5)},{scale(79.5)} {scale(66.5)},{scale(76.8)} {scale(70.7)},{scale(72.3)} "
    f"L {scale(77.2)},{scale(78.8)} "
    f"C {scale(71.2)},{scale(84.2)} {scale(63.0)},{scale(87.5)} {scale(54)},{scale(87.5)} Z"
)
draw_cmds.append(f'stroke none fill "#06B6D4" path "{p_bottom}"')

# Central Staff and Sphere (Caduceus) in crisp #F8FAFC
sphere_cx, sphere_cy = scale(54), scale(34.5)
sphere_r = scale(3.2)
draw_cmds.append(f'stroke none fill "#F8FAFC" circle {sphere_cx},{sphere_cy} {sphere_cx},{sphere_cy - sphere_r}')

# Vertical Staff
staff_path = (
    f"M {scale(52.6)},{scale(37)} "
    f"L {scale(55.4)},{scale(37)} "
    f"L {scale(55.4)},{scale(71)} "
    f"L {scale(54.0)},{scale(74)} "
    f"L {scale(52.6)},{scale(71)} Z"
)
draw_cmds.append(f'stroke none fill "#F8FAFC" path "{staff_path}"')

# Wings
# Left Wing
left_wing = (
    f"M {scale(53)},{scale(38)} "
    f"C {scale(47)},{scale(35.5)} {scale(41)},{scale(34)} {scale(36)},{scale(35.5)} "
    f"C {scale(37.5)},{scale(38)} {scale(39)},{scale(40.5)} {scale(37.5)},{scale(43)} "
    f"C {scale(41.5)},{scale(44)} {scale(46)},{scale(43.5)} {scale(49.5)},{scale(42.5)} "
    f"C {scale(51.5)},{scale(41.8)} {scale(52.8)},{scale(40)} {scale(53)},{scale(38)} Z"
)
draw_cmds.append(f'stroke none fill "#F8FAFC" path "{left_wing}"')

# Right Wing
right_wing = (
    f"M {scale(55)},{scale(38)} "
    f"C {scale(61)},{scale(35.5)} {scale(67)},{scale(34)} {scale(72)},{scale(35.5)} "
    f"C {scale(70.5)},{scale(38)} {scale(69)},{scale(40.5)} {scale(70.5)},{scale(43)} "
    f"C {scale(66.5)},{scale(44)} {scale(62)},{scale(43.5)} {scale(58.5)},{scale(42.5)} "
    f"C {scale(56.5)},{scale(41.8)} {scale(55.2)},{scale(40)} {scale(55)},{scale(38)} Z"
)
draw_cmds.append(f'stroke none fill "#F8FAFC" path "{right_wing}"')

# Serpents - Left Snake Body & Head
left_snake_body = (
    f"M {scale(49)},{scale(44.5)} "
    f"C {scale(45)},{scale(46)} {scale(43)},{scale(49)} {scale(44.5)},{scale(52.5)} "
    f"C {scale(46)},{scale(56)} {scale(52)},{scale(57)} {scale(54)},{scale(59)} "
    f"C {scale(56)},{scale(61)} {scale(61.5)},{scale(62.5)} {scale(61.5)},{scale(65.5)} "
    f"C {scale(61.5)},{scale(68)} {scale(58)},{scale(70)} {scale(54)},{scale(71)}"
)
draw_cmds.append(f'fill none stroke "#F8FAFC" stroke-width {scale(2.4)} stroke-linecap round path "{left_snake_body}"')

left_snake_head = (
    f"M {scale(47)},{scale(43)} "
    f"L {scale(50.5)},{scale(44.8)} "
    f"L {scale(48)},{scale(46.8)} Z"
)
draw_cmds.append(f'stroke none fill "#F8FAFC" path "{left_snake_head}"')

# Serpents - Right Snake Body & Head
right_snake_body = (
    f"M {scale(59)},{scale(44.5)} "
    f"C {scale(63)},{scale(46)} {scale(65)},{scale(49)} {scale(63.5)},{scale(52.5)} "
    f"C {scale(62)},{scale(56)} {scale(56)},{scale(57)} {scale(54)},{scale(59)} "
    f"C {scale(52)},{scale(61)} {scale(46.5)},{scale(62.5)} {scale(46.5)},{scale(65.5)} "
    f"C {scale(46.5)},{scale(68)} {scale(50)},{scale(70)} {scale(54)},{scale(71)}"
)
draw_cmds.append(f'fill none stroke "#F8FAFC" stroke-width {scale(2.4)} stroke-linecap round path "{right_snake_body}"')

right_snake_head = (
    f"M {scale(61)},{scale(43)} "
    f"L {scale(57.5)},{scale(44.8)} "
    f"L {scale(60)},{scale(46.8)} Z"
)
draw_cmds.append(f'stroke none fill "#F8FAFC" path "{right_snake_head}"')

with open("tmp/draw_icon.txt", "w") as f:
    f.write("\n".join(draw_cmds))

print("Created draw commands file. Total commands:", len(draw_cmds))
