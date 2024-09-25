/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Shattered Pixel Dungeon Mod
 * Copyright (C) 2024-2024 Oxtaly
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.watabou.noosa.Gizmo;

import java.util.function.Function;

public class UpdateableButton_MOD extends StyledButton {

    private Function<Button, String> getter;
    private String previousLabel;
    private final static String TEMP_LABEL_BC_I_AM_LAZY = "t3mp";

    public UpdateableButton_MOD(Function<Button, String> getter ) {
        this( getter, 9 );
    }

    public UpdateableButton_MOD(Function<Button, String> getter, int size ) {
        this( Chrome.Type.RED_BUTTON, getter, size );
    }

    public UpdateableButton_MOD(Chrome.Type type, Function<Button, String> getter, int size ){
        super( type, TEMP_LABEL_BC_I_AM_LAZY, size);
        previousLabel = TEMP_LABEL_BC_I_AM_LAZY;
        this.getter = getter;
        update();
    }

    @Override
    public void update() {
        super.update();

        String newLabel = getter.apply(this);
        if(newLabel.equals(previousLabel)) { // Same as previous label, no need to update
            return;
        }

        for (int i=0; i < length; i++) {
            Gizmo gizmo = members.get( i );
            if (gizmo != null && gizmo instanceof RenderedTextBlock) {
                RenderedTextBlock textBlock = ((RenderedTextBlock) gizmo);
                if(textBlock.text().equals(previousLabel)) {
                    textBlock.text(newLabel);
                    textBlock.update();
                    previousLabel = newLabel;
                    break;
                }
            }
        }
    }
}
