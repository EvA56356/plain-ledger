"""Check the palette shared by the native shell and packaged WebView."""
import pathlib,re,unittest,xml.etree.ElementTree as ET
ROOT=pathlib.Path(__file__).resolve().parent
class ThemeTests(unittest.TestCase):
 def test_native_and_web_palette_match(self):
  colors={c.attrib['name']:c.text.lower() for c in ET.parse(ROOT/'android/res/values/palette.xml').getroot().findall('color')}
  css='\n'.join((ROOT/'web'/f).read_text(encoding='utf-8') for f in ['style.css','appearance.css','theme.css'] if (ROOT/'web'/f).exists())
  mapping={'primary':'green','background':'bg','paper':'paper','ink':'ink','soft':'soft'}
  def normalize(s):
   s=s.lower();return '#'+''.join(c*2 for c in s[1:]) if len(s)==4 else s
  for native,web in mapping.items():
   values=re.findall(r'--'+web+r'\s*:\s*(#[0-9a-fA-F]{3,6})\s*[;}]',css)
   self.assertTrue(values,web);self.assertEqual(normalize(values[-1]),colors['ledger_'+native],web)
  styles=(ROOT/'android/res/values/styles.xml').read_text(encoding='utf-8')
  self.assertIn('@color/ledger_primary',styles);self.assertIn('@color/ledger_background',styles)
  self.assertIn('@color/ledger_primary',(ROOT/'android/res/drawable/ic_launcher.xml').read_text(encoding='utf-8'))
 def test_shared_assets_and_native_surfaces(self):
  html=(ROOT/'web/index.html').read_text(encoding='utf-8');self.assertIn('href="appearance.css"',html)
  if (ROOT/'web/theme.css').exists():self.assertLess(html.index('appearance.css'),html.index('theme.css'))
  self.assertIn('uiColor(this,"background"',(ROOT/'android/src/cn/plainledger/MainActivity.java').read_text(encoding='utf-8'))
  overlay=(ROOT/'android/src/cn/plainledger/AutoBillService.java').read_text(encoding='utf-8')
  for key in ['paper','ink','primary','soft']:self.assertRegex(overlay,r'uiColor\(\s*this\s*,\s*"'+key+'"')
  self.assertIn('MainActivity.uiRadius(this)',overlay)
if __name__=='__main__':unittest.main()
