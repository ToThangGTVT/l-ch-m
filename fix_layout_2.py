import re

with open('app/src/main/res/layout/widget_month.xml', 'r') as f:
    content = f.read()

def replacer(match):
    id_str = match.group(1)
    return f'<FrameLayout android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="1"><ImageView android:id="@+id/{id_str}_bg" android:layout_width="match_parent" android:layout_height="match_parent" android:layout_gravity="center" android:padding="2dp" android:scaleType="centerInside" android:visibility="invisible" /><TextView android:id="@+id/{id_str}" android:layout_width="match_parent" android:layout_height="match_parent" android:gravity="center" android:textColor="#1D1B1E" android:textSize="10sp" android:includeFontPadding="false" /></FrameLayout>'

regex = r'<FrameLayout[^>]*><TextView android:id="@\+id/(cell_\d_\d)"[^>]*/></FrameLayout>'
print(f"Replaced {len(re.findall(regex, content))} occurrences")
new_content = re.sub(regex, replacer, content)

with open('app/src/main/res/layout/widget_month.xml', 'w') as f:
    f.write(new_content)

